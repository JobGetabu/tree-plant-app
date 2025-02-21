package com.mobiletreeplantingapp.domain.matcher

interface EmailMatcher {
    fun isValid(email: String): Boolean
}