package eu.kanade.tachiyomi.network

import com.frostwire.jlibtorrent.SessionManager
import fi.iki.elonen.NanoHTTPD
import java.io.File

class TorrentServer(private val port: Int) : NanoHTTPD(port) {
    private val sessionManager = SessionManager()
    private var currentVideoFile: File? = null

    fun startTorrent(magnetLink: String, saveDir: File) {
        if (!sessionManager.isRunning) {
            sessionManager.start()
        }
        
        // Tells the engine to start downloading the magnet to the app's cache folder
        sessionManager.download(magnetLink, saveDir)
    }

    override fun serve(session: IHTTPSession): Response {
        // When Aniyomi's player asks for the video, this server catches the request
        val file = currentVideoFile
        
        if (file == null || !file.exists()) {
            return newFixedLengthResponse(
                Response.Status.NOT_FOUND, 
                MIME_PLAINTEXT, 
                "Buffering torrent metadata..."
            )
        }

        return newFixedLengthResponse(
            Response.Status.OK, 
            "video/mp4", 
            "Streaming video chunks..."
        )
    }

    fun stopServer() {
        sessionManager.stop()
        stop()
    }
}