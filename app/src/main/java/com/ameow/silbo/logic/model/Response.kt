package com.ameow.silbo.logic.model

data class Response(val code: String, val msg: String, val data: Map<String, String>)