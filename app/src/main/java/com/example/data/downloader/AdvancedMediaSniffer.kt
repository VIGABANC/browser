package com.example.data.downloader

object AdvancedMediaSniffer {
    const val INJECT_JS = """
        (function() {
            if (window.__aegis_sniffer_active) return;
            window.__aegis_sniffer_active = true;

            function extractMediaInfo(el) {
                var src = el.src || el.currentSrc;
                if (!src && el.tagName.toLowerCase() === 'video') {
                    var source = el.querySelector('source');
                    if (source) src = source.src;
                }
                if (!src) return null;

                var rect = el.getBoundingClientRect();
                var area = rect.width * rect.height;
                var isVisible = (rect.top >= 0 && rect.left >= 0 && rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) && rect.right <= (window.innerWidth || document.documentElement.clientWidth));

                return {
                    url: src,
                    type: el.tagName.toLowerCase() === 'audio' ? 'AUDIO' : 'VIDEO',
                    title: document.title || '',
                    duration: el.duration || 0,
                    width: el.videoWidth || rect.width || 0,
                    height: el.videoHeight || rect.height || 0,
                    isPlaying: !el.paused && !el.ended && el.readyState > 2,
                    isVisible: isVisible,
                    area: area,
                    poster: el.poster || ''
                };
            }

            function reportMedia(el) {
                var info = extractMediaInfo(el);
                if (info && info.url && !info.url.startsWith('blob:')) {
                    if (typeof AegisBridge !== 'undefined' && AegisBridge.onMediaFound) {
                        AegisBridge.onMediaFound(JSON.stringify(info));
                    }
                }
            }

            function scanDOM() {
                document.querySelectorAll('video, audio').forEach(reportMedia);
            }

            var observer = new MutationObserver(function(mutations) {
                mutations.forEach(function(mutation) {
                    mutation.addedNodes.forEach(function(node) {
                        if (node.tagName && (node.tagName.toLowerCase() === 'video' || node.tagName.toLowerCase() === 'audio')) {
                            reportMedia(node);
                            node.addEventListener('play', function() { reportMedia(node); });
                        } else if (node.querySelectorAll) {
                            node.querySelectorAll('video, audio').forEach(function(mediaNode) {
                                reportMedia(mediaNode);
                                mediaNode.addEventListener('play', function() { reportMedia(mediaNode); });
                            });
                        }
                    });
                });
            });

            observer.observe(document.body, { childList: true, subtree: true });

            document.querySelectorAll('video, audio').forEach(function(node) {
                node.addEventListener('play', function() { reportMedia(node); });
            });

            scanDOM();
        })();
    """
}
