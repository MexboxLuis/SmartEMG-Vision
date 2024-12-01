import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import kotlin.math.abs
import kotlin.math.pow

fun variationalModeDecomposition(
    x: DoubleArray,
    numIMFs: Int = 12,
    alpha: Double = 4000.0,
    tau: Double = 0.0,
    dc: Boolean = false,
    init: Int = 1,
    tol: Double = 1e-7
): Pair<Array<DoubleArray>, Array<Double>> {
    val saveT = x.size

    // Extender la señal por espejado
    val fMirror = DoubleArray(2 * saveT) { i ->
        when {
            i < saveT / 2 -> x[saveT / 2 - i - 1]
            i < 3 * saveT / 2 -> x[i - saveT / 2]
            else -> x[2 * saveT - i - 1]
        }
    }

    val T = fMirror.size
    val t = DoubleArray(T) { it.toDouble() / T }
    val freqs = t.map { it - 0.5 - 1.0 / T }.toDoubleArray()

    // FFT de la señal extendida
    val fHat = fft(fMirror)
    val fHatPlus = fHat.mapIndexed { index, value -> if (index < T / 2) Complex.ZERO else value }.toTypedArray()

    val alphaArray = DoubleArray(numIMFs) { alpha }
    val uHatPlus = Array(numIMFs) { Array(T) { Complex.ZERO } }
    val omegaPlus = Array(numIMFs) { DoubleArray(T) }

    // Inicializar omegas
    for (i in 0 until numIMFs) {
        omegaPlus[i][0] = when (init) {
            1 -> (0.5 / numIMFs) * i
            2 -> Math.random()
            else -> 0.0
        }
    }

    if (dc) omegaPlus[0][0] = 0.0

    val lambdaHat = Array(T) { Complex.ZERO }
    var uDiff = tol + 1e-10
    var n = 0
    val maxIterations = 500

    while (uDiff > tol && n < maxIterations) {
        var sumUk = Array(T) { Complex.ZERO }
        for (k in 0 until numIMFs) {
            val residual = fHatPlus.zip(sumUk) { f, s -> f.subtract(s) }
            val denom = residual.mapIndexed { i, _ ->
                1.0 + alphaArray[k] * (freqs[i] - omegaPlus[k][n]).pow(2)
            }
            uHatPlus[k] = residual.zip(denom) { res, den -> res.divide(den) }.toTypedArray()

            if (!dc || k > 0) {
                val weightedFreqs = freqs.zip(uHatPlus[k]) { f, u -> f * u.abs().pow(2) }
                omegaPlus[k][n + 1] = weightedFreqs.sum() / uHatPlus[k].sumOf { it.abs().pow(2) }
            }

            sumUk = sumUk.zip(uHatPlus[k]) { s, u -> s.add(u) }.toTypedArray()
        }

        // Actualización del término dual
        val lambdaUpdate = lambdaHat.mapIndexed { i, lambda ->
            lambda.add(Complex(tau * (sumUk[i].subtract(fHatPlus[i])).real, 0.0))
        }
        lambdaHat.indices.forEach { i -> lambdaHat[i] = lambdaUpdate[i] }

        uDiff = uHatPlus.sumOf { uk -> uk.sumOf { it.abs().pow(2) } }
        n++
    }

    // Reconstrucción de las IMFs
    val imfs = Array(numIMFs) { DoubleArray(saveT) }
    for (k in 0 until numIMFs) {
        imfs[k] = ifft(uHatPlus[k]).take(saveT).map { it.real }.toDoubleArray()
    }

    return Pair(imfs, omegaPlus.map { it[n] }.toTypedArray())
}

// Funciones auxiliares para FFT e IFFT usando Apache Commons Math
fun fft(input: DoubleArray): Array<Complex> {
    val transformer = FastFourierTransformer(DftNormalization.STANDARD)
    return transformer.transform(input, TransformType.FORWARD)
}

fun ifft(input: Array<Complex>): Array<Complex> {
    val transformer = FastFourierTransformer(DftNormalization.STANDARD)
    return transformer.transform(input, TransformType.INVERSE)
}
