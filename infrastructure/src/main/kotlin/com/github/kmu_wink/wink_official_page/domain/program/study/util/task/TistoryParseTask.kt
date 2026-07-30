package com.github.kmu_wink.wink_official_page.domain.program.study.util.task

import com.github.kmu_wink.wink_official_page.application.port.out.repository.StudyRepository
import com.github.kmu_wink.wink_official_page.domain.program.study.schema.Study
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
@ConditionalOnProperty(
    prefix = "app.program.study.tistory-sync",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class TistoryParseTask(
    private val studyRepository: StudyRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val formatter = DateTimeFormatter.ofPattern("yyyy. M. d. HH:mm")

    @EventListener(ApplicationReadyEvent::class)
    @Scheduled(cron = "0 0 6 * * *")
    fun run() {
        try {
            sync()
        } catch (exception: Exception) {
            log.error("Failed to synchronize Tistory studies", exception)
        }
    }

    private fun sync() {
        val remoteLatestIndex = getRemoteLatestIndex()
        val localLatestIndex = getLocalLatestIndex()

        for (index in (localLatestIndex + 1)..remoteLatestIndex) {
            try {
                val document = Jsoup.connect("https://cs-kookmin-club.tistory.com/$index").get()
                val category = transferWinkCategory(document) ?: continue
                val title = text(document, "#content > div > div.hgroup > h1")
                val author = text(document, "#content > div > div.hgroup > div.post-meta > span.author")
                val rawDate = text(document, "#content > div > div.hgroup > div.post-meta > span.date")
                val date = LocalDateTime.parse(rawDate, formatter)
                var content = text(document, "#content > div > div.entry-content > div.contents_style")
                val image = firstImage(document, "#content > div > div.entry-content > div.contents_style")
                content = content.replace(Regex("[\\s\\n\\t]+"), " ").trim().let { it.substring(0, minOf(it.length, 300)) }

                val study = Study(
                    createdAt = date,
                    updatedAt = date,
                    index = index,
                    category = category,
                    title = title,
                    author = author,
                    content = content,
                    image = image,
                )
                studyRepository.save(study)
                log.info("New post saved. (index={}, title={})", index, title)
            } catch (exception: IOException) {
                log.warn("Failed to sync Tistory post. (index={})", index, exception)
            }
        }
    }

    @Throws(IOException::class)
    private fun getRemoteLatestIndex(): Int {
        val latestDocument = Jsoup.connect("https://cs-kookmin-club.tistory.com/category/WINK-%28Web%20%26%20App%29").get()
        val latestElement = requireNotNull(latestDocument.selectFirst("#content > div.inner > div:nth-child(1) > a"))
        val latestUrl = latestElement.attr("href")
        return latestUrl.substring(latestUrl.lastIndexOf("/") + 1).toInt()
    }

    private fun getLocalLatestIndex(): Int =
        studyRepository.findTopByOrderByIndexDesc().orElseGet { Study(index = 0) }.index

    private fun transferWinkCategory(document: Document): String? {
        val category = requireNotNull(document.selectFirst("#content > div > div.hgroup > div.category")).text()
        if (!category.startsWith("WINK-(Web & App)/")) {
            return null
        }
        return category.substring("WINK-(Web & App)/".length)
    }

    private fun text(document: Document, selector: String): String =
        requireNotNull(document.selectFirst(selector)).text().trim()

    private fun firstImage(document: Document, selector: String): String? =
        requireNotNull(document.selectFirst(selector))
            .select("img")
            .asSequence()
            .map { it.attr("src") }
            .filter { it.isNotBlank() }
            .firstOrNull { it.startsWith("http") }
}
