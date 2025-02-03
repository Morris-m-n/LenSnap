//package com.lensnap.app.data
//
//import android.graphics.Bitmap
//import android.opengl.EGLConfig
//import android.opengl.GLES20
//import android.opengl.GLSurfaceView
//import jp.co.cyberagent.android.gpuimage.GPUImage
//import javax.microedition.khronos.opengles.GL10
//import java.nio.IntBuffer
//
//class CustomRenderer(private val onBitmapCaptured: (Bitmap) -> Unit) : GLSurfaceView.Renderer {
//    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
//        // Initialize OpenGL settings if needed
//        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
//    }
//
//    override fun onDrawFrame(gl: GL10?) {
//        // Clear screen and draw the current frame
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
//
//        // Capture the current frame as a bitmap
//        val width = gl?.glGetIntegerv(GLES20.GL_VIEWPORT, IntBuffer.allocate(4))?.get(2) ?: 0
//        val height = gl?.glGetIntegerv(GLES20.GL_VIEWPORT, IntBuffer.allocate(4))?.get(3) ?: 0
//        if (width > 0 && height > 0) {
//            val buffer = IntArray(width * height)
//            val intBuffer = IntBuffer.wrap(buffer)
//            intBuffer.position(0)
//            GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, intBuffer)
//            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//            bitmap.setPixels(buffer, 0, width, 0, 0, width, height)
//            onBitmapCaptured(bitmap)
//        }
//    }
//
//    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
//        // Adjust the viewport if the surface dimensions change
//        GLES20.glViewport(0, 0, width, height)
//    }
//}
