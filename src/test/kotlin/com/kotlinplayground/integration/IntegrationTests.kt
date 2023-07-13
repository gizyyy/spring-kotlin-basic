package com.kotlinplayground.integration

import com.jayway.jsonpath.matchers.JsonPathMatchers
import com.kotlinplayground.domain.School
import com.kotlinplayground.domain.externalevents.SchoolRegisteredEvent
import junit.framework.TestCase.*
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.InputDestination
import org.springframework.cloud.stream.binder.test.OutputDestination
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.IOException

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
@Import(TestChannelBinderConfiguration::class)
class IntegrationTests{

    @Autowired
    private val inputDestination: InputDestination? = null
    @Autowired
    private val outputDestination: OutputDestination? = null

    init {
        mongoDBContainer.withNetworkAliases("mongo")
            .withExposedPorts(27017).start()
    }

    companion object {
        private val mongoDBContainer = MongoDBContainer("mongo:6.0.4")
    }

    @Test
    @Throws(IOException::class)
    fun whenSchoolRegisteredEventSentInternalEventShouldBeEmitted() {
        val schoolRegisteredEvent = SchoolRegisteredEvent(
            School(
                "1",
                "adsiz",
                arrayListOf(),
                arrayListOf()
            )
        )
        val headersMap = mutableMapOf<String, Any>()
        headersMap["ce_type"] = SchoolRegisteredEvent.type
        headersMap["partitionKey"] = schoolRegisteredEvent.school.schoolId
        val headers = MessageHeaders(headersMap)

        inputDestination!!.send(
            MessageBuilder.createMessage(schoolRegisteredEvent, headers),
            "external.education.events.schools"
        )

        // Asserting emitted event
        val eventJson = outputDestination!!.receive(2000, "internal.education.events.schools").payload
        val payload = String(eventJson)
        MatcherAssert.assertThat(
            payload,
            JsonPathMatchers.hasJsonPath("$.id", Matchers.notNullValue())

        )

        MatcherAssert.assertThat(
            payload,
            JsonPathMatchers.hasJsonPath(
                "$.ce_type",
                Matchers.equalTo("education.service.events.external.students.StudentRegisteredEvent")
            )
        )

        MatcherAssert.assertThat(
            payload,
            JsonPathMatchers.hasJsonPath("$.datacontenttype", Matchers.equalTo("application/json"))
        )
        MatcherAssert.assertThat(
            payload,
            JsonPathMatchers.hasJsonPath("$.data.courierId", Matchers.equalTo("13375275"))
        )
    }

}