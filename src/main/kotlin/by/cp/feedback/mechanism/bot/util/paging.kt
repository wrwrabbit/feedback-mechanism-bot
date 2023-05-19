package by.cp.feedback.mechanism.bot.util

import kotlin.math.ceil

data class PagedDto<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalElements: Long,
)

fun toLimitOffsetTotalPages(page: Int, size: Int, totalElements: Long): Triple<Int, Long, Int> {
    val floorPage = if ((totalElements % size) == 0L) {
        0
    } else {
        1
    }
    val totalPages = ceil((totalElements / size).toDouble()) + floorPage
    val limit = size
    val offset = (page - 1) * size
    return Triple(limit, offset.toLong(), totalPages.toInt())
}
