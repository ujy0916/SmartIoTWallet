// Fill in  your WiFi networks SSID and password
#define SECRET_SSID "N10+Yangyu"
#define SECRET_PASS "1791234yangyu*"

// Fill in the hostname of your AWS IoT broker
#define SECRET_BROKER "a19613o2uxxm5b-ats.iot.ap-northeast-2.amazonaws.com"

// Fill in the boards public certificate
const char SECRET_CERTIFICATE[] = R"(
-----BEGIN CERTIFICATE-----
MIICpjCCAY6gAwIBAgIVALcYYPKEPalz8Gz2iiYfWPzNjjxnMA0GCSqGSIb3DQEB
CwUAME0xSzBJBgNVBAsMQkFtYXpvbiBXZWIgU2VydmljZXMgTz1BbWF6b24uY29t
IEluYy4gTD1TZWF0dGxlIFNUPVdhc2hpbmd0b24gQz1VUzAeFw0yMDExMDUwMjQ3
MjNaFw00OTEyMzEyMzU5NTlaMDUxFjAUBgNVBAYTDU15TUtSV2lGaTEwMTAxGzAZ
BgNVBAMTEjAxMjNBMzUyMjIwMzE5QzdFRTBZMBMGByqGSM49AgEGCCqGSM49AwEH
A0IABFz3NpySUJjryGbgqOB+JDp8Jp1A6jXaUEOstxkrXkWdHw+x/jNA952McLFD
qTd2NRPTd+Tg2NFM5uUrQqf3acajYDBeMB8GA1UdIwQYMBaAFO8p5sRX1B+deIb9
KrGvBi3HzYOgMB0GA1UdDgQWBBRtTb84dWCNieA/vW6D/JmVJAQfgDAMBgNVHRMB
Af8EAjAAMA4GA1UdDwEB/wQEAwIHgDANBgkqhkiG9w0BAQsFAAOCAQEAlpiQyzoT
h9Bg+vI/95BEpYmftMF9K2sRiUlx9/JHu6c4btZu8kg40LE6Keu0awWfMehuDl8R
F4mnXrOw6Zv1O934iwWdnIyAKiQ4R319wXlHK8SZChiQ/3Jhmelr931h4M2GcnWC
i/mHYQxDLcO5nw2qzEWXl4pSejKxc/F9Sufb639xdEZ+jBFWkAdhBO18kVgBA0qz
2sJ4eP89Zq/kF1Uyz/jVcC+z3e52aPBl13tfcP4R+izGbnDuCNgM7CeUsArU60WO
m/Ty7T5OtCLweLV801xUZNYdICgs+dDpGJkSBRZBQ5ptpz57RHzNYm9K3Lh4hPF+
YFQYyZYxbtmbjw==
-----END CERTIFICATE-----
)";