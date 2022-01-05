
/**
 * @date on 2021/12/28
 * @author huanghui
 * @title
 * @describe
 */

object Versions {
    val appcompat = "1.2.0"
    val coreKtx = "1.3.2"
    val constraintlayout = "2.0.4"
    val kotlin = "1.4.20"
    val coroutine = "1.3.9"
}

object AndroidX {
    val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    val constraintlayout =
        "androidx.constraintlayout:constraintlayout:${Versions.constraintlayout}"
    val material = "com.google.android.material:material:1.1.0"
}

object Kt {
    val stdlibJdk7 = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    val stdlibJdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    val test = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlin}"
    val plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    val coroutineCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutine}"
    val coroutineAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutine}"
}

object Depend{
    val startupApi = "io.github.cnoke.startup:api:1.1.0"
}
