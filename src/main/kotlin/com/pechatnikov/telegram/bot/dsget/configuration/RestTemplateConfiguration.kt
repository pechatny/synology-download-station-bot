package com.pechatnikov.telegram.bot.dsget.configuration

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate
import org.springframework.web.filter.CommonsRequestLoggingFilter

@Configuration
class RestTemplateConfiguration {

    @Bean
    fun getRestTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate{
        return restTemplateBuilder.build()
    }

    @Bean
    fun jsonConverter(): MappingJackson2HttpMessageConverter{
        val converter = MappingJackson2HttpMessageConverter()
        converter.supportedMediaTypes = listOf(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON)

        return converter
    }

//    @Bean
//    fun requestLoggingFilter(): CommonsRequestLoggingFilter? {
//        val loggingFilter = CommonsRequestLoggingFilter()
//        loggingFilter.setIncludeClientInfo(true)
//        loggingFilter.setIncludeQueryString(true)
//        loggingFilter.setIncludePayload(true)
//        loggingFilter.setIncludeHeaders(false)
//        return loggingFilter
//    }
//    @Bean
//    fun stringConverter(): StringHttpMessageConverter{
//        val converter = StringHttpMessageConverter()
//        converter.supportedMediaTypes = listOf(MediaType.TEXT_PLAIN, MediaType.MULTIPART_FORM_DATA)
//
//        return converter
//    }
}