package com.example.codise.utils

import com.example.codise.data.ApiService

fun String.toFullUrl(): String {
    return if (this.startsWith("/")) {
        ApiService.BASE_URL.removeSuffix("/") + this
    } else {
        this
    }
}

fun extractYoutubeVideoId(url: String): String? {
    val pattern = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*"
    val compiledPattern = java.util.regex.Pattern.compile(pattern)
    val matcher = compiledPattern.matcher(url)
    return if (matcher.find()) {
        matcher.group()
    } else {
        null
    }
}

fun getYoutubeThumbnailUrl(videoId: String): String {
    return "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
}
