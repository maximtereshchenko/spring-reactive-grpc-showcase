#!/usr/bin/env bash

set -Eeuo pipefail

if [ "${1-}" = "aws" ]; then
  HOST=$(terraform -chdir=./terraform output --raw "api_gateway_dns")
else
  HOST="http://localhost:8080"
fi

printf "register as Admin\n"
curl -i -X POST -H "Content-Type:application/json" "${HOST}/users" -d '{"username":"admin","password":"pass","userType":"ADMIN"}'
sleep 5s

printf "\nlogin as Admin\n"
ADMIN_TOKEN=$(curl -X POST -H "Content-Type:application/json" "${HOST}/users/login" -d '{"username":"admin","password":"pass"}')
echo "$ADMIN_TOKEN"
sleep 5s

printf "\nregister as User\n"
curl -i -X POST -H "Content-Type:application/json" "${HOST}/users" -d '{"username":"user","password":"pass","userType":"REGULAR"}'
sleep 5s

printf "\nlogin as User\n"
USER_TOKEN=$(curl -X POST -H "Content-Type:application/json" "${HOST}/users/login" -d '{"username":"user","password":"pass"}')
echo "$USER_TOKEN"
sleep 5s

printf "\ncreate an item\n"
ITEM=$(curl -X POST -H "Content-Type:application/json" -H "Authorization:${ADMIN_TOKEN}" "${HOST}/items" -d "item")
echo "$ITEM"

printf "\nview all items\n"
curl -i -X GET "${HOST}/items"
sleep 5s

printf "\ndeactivate an item\n"
curl -i -X POST -H "Authorization:${ADMIN_TOKEN}" "${HOST}/items/${ITEM}/deactivate"
sleep 5s

printf "\nactivate an item\n"
curl -i -X POST -H "Authorization:${ADMIN_TOKEN}" "${HOST}/items/${ITEM}/activate"
sleep 5s

printf "\nadd item to cart\n"
curl -i -X POST -H "Content-Type:application/json" -H "Authorization:${USER_TOKEN}" "${HOST}/cart/add" -d "{\"itemId\":\"${ITEM}\",\"quantity\":\"2\"}"
sleep 5s

printf "\nremove item from cart\n"
curl -i -X POST -H "Content-Type:application/json" -H "Authorization:${USER_TOKEN}" "${HOST}/cart/remove" -d "{\"itemId\":\"${ITEM}\",\"quantity\":\"1\"}"
sleep 5s

printf "\nview items in cart\n"
curl -i -X GET -H "Authorization:${USER_TOKEN}" "${HOST}/cart"
sleep 5s

printf "\nmake an order\n"
curl -i -X POST -H "Authorization:${USER_TOKEN}" "${HOST}/cart/order"
sleep 5s

printf "\nview orders\n"
curl -i -X GET -H "Authorization:${USER_TOKEN}" "${HOST}/orders"
sleep 5s

printf "\nview top ordered items\n"
curl -i -X GET -H "Authorization:${ADMIN_TOKEN}" "${HOST}/items/top"
sleep 5s
