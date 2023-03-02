#!/usr/bin/env bash

set -Eeuo pipefail

openssl genrsa -aes256 -passout pass:password -out ca-private-key.key 4096
openssl req -new -key ca-private-key.key -passin pass:password -subj "/CN=Certificate authority/" -out ca-certificate-signing-request.csr
openssl x509 -req -in ca-certificate-signing-request.csr -signkey ca-private-key.key -passin pass:password -days 365 -out ca-self-signed-certificate.pem
openssl genrsa -aes256 -out server-private-key.key -passout pass:password 4096
openssl req -new -key server-private-key.key -passin pass:password -subj "/CN=*.amazonaws.com/" -addext "subjectAltName=DNS:localhost,DNS:users,DNS:orders-read-side,DNS:orders-write-side" -out server-certificate-signing-request.csr
openssl x509 -req -in server-certificate-signing-request.csr -CA ca-self-signed-certificate.pem -CAkey ca-private-key.key -passin pass:password -CAcreateserial -days 365 -out server-certificate.pem -extfile <(echo "subjectAltName=DNS:localhost,DNS:users,DNS:orders-read-side,DNS:orders-write-side")
openssl rsa -in server-private-key.key -passin pass:password -out server-private-key-no-pass.key
openssl pkcs8 -topk8 -nocrypt -in server-private-key-no-pass.key -out server-private-key-pkcs8.key

mv server-private-key-pkcs8.key ./common/src/main/resources/server-private-key-pkcs8.key
mv server-certificate.pem ./common/src/main/resources/server-certificate.pem
mv ca-self-signed-certificate.pem ./common/src/main/resources/ca-self-signed-certificate.pem

rm ca-certificate-signing-request.csr ca-private-key.key ca-self-signed-certificate.srl server-certificate-signing-request.csr server-private-key.key server-private-key-no-pass.key