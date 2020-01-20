package io.richardtin.myglsurface

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

// number of coordinates per vertex in this array
var squareCoords = floatArrayOf(
    -0.5f, 0.5f, 0.0f,      // top left
    -0.5f, -0.5f, 0.0f,      // bottom left
    0.5f, -0.5f, 0.0f,      // bottom right
    0.5f, 0.5f, 0.0f       // top right
)

class Square {

    private var positionHandle: Int = 0
    private var colorHandle: Int = 0

    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    private val vertexShaderCode =
        "attribute vec4 vPosition;" +
                "void main() {" +
                "  gl_Position = vPosition;" +
                "}"

    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"

    // Set color with red, green, blue and alpha (opacity) values
    private val color = floatArrayOf(0.2f, 0.709803922f, 0.898039216f, 1.0f)

    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3) // order to draw vertices

    // initialize vertex byte buffer for shape coordinates
    private val vertexBuffer: FloatBuffer =
        // (# of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(squareCoords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(squareCoords)
                position(0)
            }
        }

    // initialize byte buffer for the draw list
    private val drawListBuffer: ShortBuffer =
        // (# of coordinate values * 2 bytes per short)
        ByteBuffer.allocateDirect(drawOrder.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(drawOrder)
                position(0)
            }
        }

    private var glProgram: Int

    init {

        val vertexShader: Int = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int =
            MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // create empty OpenGL ES Program
        glProgram = GLES20.glCreateProgram().also {

            // add the vertex shader to program
            GLES20.glAttachShader(it, vertexShader)

            // add the fragment shader to program
            GLES20.glAttachShader(it, fragmentShader)

            // creates OpenGL ES program executables
            GLES20.glLinkProgram(it)
        }
    }

    fun draw() {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(glProgram)

        // get handle to vertex shader's vPosition member
        positionHandle = GLES20.glGetAttribLocation(glProgram, "vPosition").also {

            // Enable a handle to the square vertices
            GLES20.glEnableVertexAttribArray(it)

            // Prepare the square coordinate data
            GLES20.glVertexAttribPointer(
                it,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )

            // get handle to fragment shader's vColor member
            colorHandle = GLES20.glGetUniformLocation(glProgram, "vColor").also { location ->

                // Set color for drawing the square
                GLES20.glUniform4fv(location, 1, color, 0)
            }

            // Draw the square
            GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.size,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer
            )

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(it)
        }
    }

    companion object {
        // number of coordinates per vertex in this array
        private const val COORDS_PER_VERTEX = 3
    }
}
