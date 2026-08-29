package com.example.data.reader

import com.example.data.model.ReaderArticle

object ReaderModeExtractor {

    const val READER_EXTRACTION_JS = """
        (function() {
            try {
                var articleElem = document.querySelector('article') || 
                                  document.querySelector('[role="main"]') || 
                                  document.querySelector('.post-content') || 
                                  document.querySelector('.article-body') || 
                                  document.querySelector('.entry-content') || 
                                  document.querySelector('#content') || 
                                  document.body;
                
                var titleElem = document.querySelector('h1') || document.querySelector('meta[property="og:title"]');
                var title = titleElem ? (titleElem.innerText || titleElem.getAttribute('content') || document.title) : document.title;
                
                var authorElem = document.querySelector('meta[name="author"]') || 
                                 document.querySelector('.byline') || 
                                 document.querySelector('.author');
                var author = authorElem ? (authorElem.getAttribute('content') || authorElem.innerText || '') : '';
                
                var siteElem = document.querySelector('meta[property="og:site_name"]');
                var siteName = siteElem ? siteElem.getAttribute('content') : window.location.hostname;
                
                var imgElem = document.querySelector('meta[property="og:image"]') || 
                              document.querySelector('article img') || 
                              document.querySelector('img');
                var leadImage = imgElem ? (imgElem.getAttribute('content') || imgElem.src || '') : '';
                
                var paragraphs = [];
                var pNodes = articleElem.querySelectorAll('p, h2, h3, blockquote');
                for (var i = 0; i < pNodes.length; i++) {
                    var txt = pNodes[i].innerText.trim();
                    if (txt.length > 25) {
                        paragraphs.push(txt);
                    }
                }
                
                if (paragraphs.length === 0) {
                    var bodyText = articleElem.innerText.trim();
                    var rawSplit = bodyText.split('\n\n');
                    for (var j = 0; j < rawSplit.length; j++) {
                        var p = rawSplit[j].trim();
                        if (p.length > 25) paragraphs.push(p);
                    }
                }
                
                var payload = {
                    title: title,
                    byline: author,
                    siteName: siteName,
                    leadImage: leadImage,
                    paragraphs: paragraphs
                };
                
                if (window.AegisBridge && window.AegisBridge.onReaderContentExtracted) {
                    window.AegisBridge.onReaderContentExtracted(JSON.stringify(payload));
                }
                return JSON.stringify(payload);
            } catch(e) {
                return '{}';
            }
        })();
    """

    fun parseArticle(
        pageTitle: String,
        pageUrl: String,
        rawPageText: String,
        jsonPayload: String? = null
    ): ReaderArticle {
        var title = pageTitle
        var byline: String? = null
        var siteName: String? = pageUrl.removePrefix("https://").removePrefix("http://").removePrefix("www.").substringBefore("/")
        var leadImage: String? = null
        val paragraphs = mutableListOf<String>()

        if (!jsonPayload.isNullOrBlank()) {
            try {
                val json = org.json.JSONObject(jsonPayload)
                title = json.optString("title", pageTitle).ifBlank { pageTitle }
                byline = json.optString("byline", "").takeIf { it.isNotBlank() }
                siteName = json.optString("siteName", siteName ?: "").takeIf { it.isNotBlank() } ?: siteName
                leadImage = json.optString("leadImage", "").takeIf { it.isNotBlank() && (it.startsWith("http://") || it.startsWith("https://")) }
                
                val pArray = json.optJSONArray("paragraphs")
                if (pArray != null) {
                    for (i in 0 until pArray.length()) {
                        val p = pArray.getString(i).trim()
                        if (p.isNotBlank()) {
                            paragraphs.add(p)
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        if (paragraphs.isEmpty() && rawPageText.isNotBlank()) {
            val lines = rawPageText.split("\n\n", "\n")
            for (line in lines) {
                val clean = line.trim()
                if (clean.length > 30) {
                    paragraphs.add(clean)
                }
            }
        }

        if (paragraphs.isEmpty()) {
            paragraphs.add(rawPageText.ifBlank { "Article content preview for $pageTitle.\n\nOpen original web view to read interactive content or dynamic elements." })
        }

        val allWords = paragraphs.joinToString(" ").split("\\s+".toRegex()).filter { it.isNotBlank() }
        val wordCount = allWords.size.coerceAtLeast(1)
        val readingTime = (wordCount / 200).coerceAtLeast(1)

        return ReaderArticle(
            title = title,
            byline = byline,
            siteName = siteName,
            excerpt = paragraphs.firstOrNull()?.take(180),
            paragraphs = paragraphs,
            wordCount = wordCount,
            estimatedReadingTimeMinutes = readingTime,
            leadImageUrl = leadImage,
            sourceUrl = pageUrl,
            rawText = paragraphs.joinToString("\n\n")
        )
    }
}
