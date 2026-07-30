dependencies {
    implementation(project(":application"))
    api(project(":domain"))
    api(project(":shared"))

    implementation(platform(libs.awspring.cloud.dependencies))
    implementation(platform(libs.aws.bom))

    implementation(libs.java.jwt)
    implementation(libs.jsoup)
    implementation(libs.spring.boot.starter.data.mongodb)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.mail)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.cloud.aws.starter.s3)
    implementation(libs.springdoc.openapi.webmvc.ui)
    implementation(libs.sentry.core)
}
