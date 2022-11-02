****spring.datasource.url=jdbc:postgresql://localhost:5432/postgres****

****init DB: src/main/resources/sql/init.sql****

****test:****

****idempotency (repeated runs always return same response)****

curl --header "Content-Type: application/json" --header "Idempotency-Key: 12345" --request POST --data '{"abcdef"}' http://localhost:8080/v1/3 --verbose

****Usual run:****

curl --header "Content-Type: application/json" --header "Idempotency-Key: 12345" --request PUT --data '{"abcdef"}' http://localhost:8080/v1/3 --verbose

