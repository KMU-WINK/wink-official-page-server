dependencies {
    api(project(":domain"))
    api(project(":shared"))

    implementation(libs.jackson.annotations)
    implementation(libs.jadenticon)
    implementation(libs.spring.boot.starter.validation)
}
