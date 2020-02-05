package io.richardtin.myglsurface

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer : GLSurfaceView.Renderer {

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)

    // Screen size
    private var screenWidth: Int = 0
        set(value) {
            field = value
            screenCenterX = value.toFloat() / 2f
        }
    private var screenHeight: Int = 0
        set(value) {
            field = value
            screenCenterY = value.toFloat() / 2f
        }
    private var screenCenterX = 0f
    private var screenCenterY = 0f

    // FPS
    private var frameCountingStart = 0L
    private var frameCount: Int = 0

    @Volatile
    var angle: Float = 0f

    private lateinit var triangle: Triangle
    private lateinit var square: Square
    private lateinit var path: Path

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)

        // initialize a triangle
        triangle = Triangle()
        // initialize a square
        square = Square()
        // initialize a path
        path = Path()
    }

    override fun onDrawFrame(unused: GL10) {
        outputFps()

        val scratch = FloatArray(16)

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Draw square
        square.draw(vPMatrix)

        // Create a rotation transformation for the triangle
        Matrix.setRotateM(rotationMatrix, 0, angle, 0f, 0f, -1.0f)

        // Combine the rotation matrix with the projection and camera view
        // Note that the vPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0)

        // Draw triangle
        triangle.draw(scratch)

        // Draw path
        path.draw(vPMatrix)
    }

    fun resetPathCoords() {
        path.pathCoords = floatArrayOf()
    }

    fun appendPointToPathCoords(x: Float, y: Float, z: Float) {
        path.pathCoords = path.pathCoords.plus(
            floatArrayOf(
                (screenCenterX - x) / screenWidth * 3.65f,
                (screenCenterY - y) / screenHeight * 2.1f,
                z
            )
        )
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        Log.d(TAG, "onSurfaceChanged(): width = $width, height = $height")

        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    private fun outputFps() {
        val now = System.nanoTime()
        if (frameCountingStart == 0L) {
            frameCountingStart = now
        } else if (now - frameCountingStart > 1000000000) {
            Log.d(TAG, "fps: ${frameCount.toDouble() * 1000_000_000 / (now - frameCountingStart)}")
            frameCountingStart = now
            frameCount = 0
        }
        ++frameCount
    }

    companion object {
        private val TAG = MyGLRenderer::class.java.simpleName

        fun loadShader(type: Int, shaderCode: String): Int {

            // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
            return GLES20.glCreateShader(type).also { shader ->

                // add the source code to the shader and compile it
                GLES20.glShaderSource(shader, shaderCode)
                GLES20.glCompileShader(shader)
            }
        }
    }
}
