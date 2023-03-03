resource "aws_acm_certificate" "server_certificate" {
  private_key      = file("../common/src/main/resources/server-private-key-pkcs8.key")
  certificate_body = file("../common/src/main/resources/server-certificate.pem")
}