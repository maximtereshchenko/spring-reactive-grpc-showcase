# What is this?

This is a repository to try out gRPC and Spring Reactor technologies.

# How the project is looking?

![](structure.png)
There will be 4 "Microservices" (not really, but they will be deployed separately as Docker containers). I will
utilize Ports and Adapters architecture, which will enable TDD approach in developing business logic. I will try CQRS
pattern to separate write side from read side for functionality related to creating orders and managing items to buy.

"API Gateway" is responsible for reading ReST request, asking "Users" service to decipher JWT and routing request to
"Orders (write side)" to perform some action or "Orders (read side)" to query some information.

"Users" is responsible for creating new users, logging in by username and password, deciphering user's information from
JWT.

"Orders (write side)" is responsible for creating new items for purchase, activating/deactivating items, adding/removing
items to/from user's cart, ordering items in cart.

"Orders (read side)" is responsible for viewing information about available items, user's cart, previously ordered
items, top ordered items.

To build these services Spring Reactor will be used alongside with gRPC for communication between them. Apache
Kafka and MongoDB were chosen mostly of their reactive driver support.

# User stories

1) Admin can create new item for purchase.
2) Admin can deactivate active item.
3) Admin can activate deactivated item.
4) Regular user can add active item to cart.
5) Regular user can remove item from cart.
6) Regular user can order items in cart, if it contains any.
7) Any user can view all items.
8) Regular user can view his cart.
9) Admin can view top ordered items.
10) Regular user can view his previous ordered items.

# API

```bash
curl -X POST \
-H 'Content-Type:application/json' \
http://localhost:8080/users \
-d '{"username":"user","password":"pass","userType":"ADMIN"}'
```

```bash
curl -X POST \
-H 'Content-Type:application/json' \
http://localhost:8080/users/login \
-d '{"username":"user","password":"pass"}'
```

```bash
curl -X POST \
-H 'Content-Type:application/json' \
-H 'Authorization:jwt' \
http://localhost:8080/items \
-d 'item'
```

# What have I learnt?

* How to organize aggregates in case of Event Sourcing.

# Out of scope concerns

* Optimistic locking, regarding events with the same version.
* Separation of writing events to database and their publishing, which can lead to interested parties not knowing about
  something important happened. Can be resolved with Outbox pattern, I guess.