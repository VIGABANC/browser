// aegis-sniffer.js — injected into every page
(function() {
    if (window.__aegisSnifferInstalled) return;
    window.__aegisSnifferInstalled = true;

    const detected = new Map(); // id -> mediaInfo

    // 1. Hook MediaSource API (catches blob/DASH players)
    const OriginalMediaSource = window.MediaSource;
    if (OriginalMediaSource) {
        window.MediaSource = function(...args) {
            const ms = new OriginalMediaSource(...args);
            const origAddSourceBuffer = ms.addSourceBuffer;
            ms.addSourceBuffer = function(mimeType) {
                const sb = origAddSourceBuffer.call(ms, mimeType);
                // Capture segments as they're appended
                const origAppend = sb.appendBuffer;
                sb.appendBuffer = function(data) {
                    reportMedia({
                        type: 'mediasource',
                        mimeType: mimeType,
                        size: data.byteLength,
                        timestamp: Date.now()
                    });
                    return origAppend.call(sb, data);
                };
                return sb;
            };
            return ms;
        };
    }

    // 2. Hook URL.createObjectURL (catches blob URLs)
    const origCreateObjectURL = URL.createObjectURL;
    URL.createObjectURL = function(blob) {
        const url = origCreateObjectURL.call(URL, blob);
        if (blob && blob.type && blob.type.startsWith('video/')) {
            reportMedia({ type: 'blob', url: url, mimeType: blob.type, size: blob.size });
        }
        return url;
    };

    // 3. Scan DOM for video/audio elements
    function scanMediaElements() {
        const elements = document.querySelectorAll('video, audio');
        elements.forEach((el, idx) => {
            const src = el.currentSrc || el.src;
            const sources = Array.from(el.querySelectorAll('source')).map(s => s.src);
            const poster = el.poster;
            const rect = el.getBoundingClientRect();

            if (src || sources.length > 0) {
                reportMedia({
                    type: 'dom',
                    element: el.tagName.toLowerCase(),
                    src: src,
                    sources: sources,
                    poster: poster,
                    width: el.videoWidth || el.offsetWidth,
                    height: el.videoHeight || el.offsetHeight,
                    duration: el.duration,
                    visible: rect.width > 0 && rect.height > 0,
                    pageUrl: location.href,
                    title: document.title
                });
            }
        });
    }

    // 4. Scan for HLS/DASH manifests in page source/scripts
    function scanForManifests() {
        const html = document.documentElement.innerHTML;
        const m3u8Matches = html.match(/https?:\/\/[^\s"']+\.m3u8[^\s"']*/g) || [];
        const mpdMatches = html.match(/https?:\/\/[^\s"']+\.mpd[^\s"']*/g) || [];
        [...m3u8Matches, ...mpdMatches].forEach(url => {
            reportMedia({ type: 'manifest', url: url, pageUrl: location.href });
        });
    }

    // 5. Scan iframes recursively
    function scanIframes() {
        document.querySelectorAll('iframe').forEach(iframe => {
            try {
                const iframeDoc = iframe.contentDocument || iframe.contentWindow.document;
                if (iframeDoc) {
                    // Post message to parent with iframe media
                    iframeDoc.querySelectorAll('video, audio').forEach(el => {
                        reportMedia({
                            type: 'iframe',
                            src: el.src || el.currentSrc,
                            pageUrl: iframe.src,
                            parentUrl: location.href
                        });
                    });
                }
            } catch (e) { /* cross-origin, ignore */ }
        });
    }

    // 6. Watch for dynamic changes
    const observer = new MutationObserver((mutations) => {
        let shouldScan = false;
        mutations.forEach(m => {
            m.addedNodes.forEach(node => {
                if (node.tagName === 'VIDEO' || node.tagName === 'AUDIO' ||
                    (node.querySelectorAll && node.querySelectorAll('video, audio').length > 0)) {
                    shouldScan = true;
                }
            });
        });
        if (shouldScan) {
            setTimeout(() => { scanMediaElements(); scanIframes(); }, 500);
        }
    });
    observer.observe(document.body, { childList: true, subtree: true });

    // 7. Report to native layer
    function reportMedia(info) {
        const id = btoa(info.src || info.url || JSON.stringify(info)).slice(0, 32);
        if (detected.has(id)) return;
        detected.set(id, info);

        // Send to Android via JS bridge
        if (window.AegisBridge && window.AegisBridge.onMediaDetected) {
            window.AegisBridge.onMediaDetected(JSON.stringify(info));
        }
    }

    // Initial scan
    scanMediaElements();
    scanForManifests();
    scanIframes();

    // Re-scan on significant events
    window.addEventListener('scroll', debounce(() => { scanMediaElements(); scanIframes(); }, 1000));
    window.addEventListener('click', () => { setTimeout(scanMediaElements, 1000); });

    function debounce(fn, ms) {
        let t; return () => { clearTimeout(t); t = setTimeout(fn, ms); };
    }
})();
