package com.github.kmu_wink.wink_official_page.global.module.email

interface EmailTemplate {
    fun getTitle(): String

    fun getHtml(): String

    fun escapeHtml(value: Any?): String =
        value?.toString()
            ?.replace("&", "&amp;")
            ?.replace("<", "&lt;")
            ?.replace(">", "&gt;")
            ?.replace("\"", "&quot;")
            ?.replace("'", "&#39;")
            .orEmpty()

    fun container(html: String): String =
        """
        <html>
            <head>
                <style>
                    .email-container {
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #ffffff;
                        padding: 20px;
                        font-family: Arial, sans-serif;
                    }

                    .header {
                        text-align: center;
                        padding-bottom: 20px;
                        border-bottom: 1px solid #dddddd;
                    }

                    .logo {
                        max-width: 150px;
                    }

                    .content {
                        padding: 20px;
                        text-align: center;
                    }

                    .title {
                        color: #333333;
                        margin-bottom: 20px;
                    }

                    .text {
                        font-size: 16px;
                        color: #666666;
                        margin: 10px 0;
                    }

                    .button {
                        display: inline-block;
                        font-size: 18px;
                        color: #ffffff !important;
                        background-color: #3a70ff;
                        padding: 14px 24px;
                        border-radius: 12px;
                        text-decoration: none;
                        margin-top: 20px;
                        border: none;
                        cursor: pointer;
                    }

                    @media screen and (max-width: 480px) {
                        .title {
                            font-size: 24px;
                        }

                        .text {
                            font-size: 14px;
                        }

                        .button {
                            font-size: 16px;
                            padding: 12px 20px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="header">
                        <img src="https://i.imgur.com/qXRRE56.png" alt="Wink Logo" class="logo">
                    </div>
                    <div class="content">
                        %s
                    </div>
                </div>
            </body>
        </html>
        """.trimIndent().format(html)
}
