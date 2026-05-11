#!/bin/bash

# Wait for postgres to be ready
echo "Waiting for PostgreSQL to be ready..."
until pg_isready -h postgres-db -U irms_user; do
  sleep 2
done

echo "PostgreSQL is ready. Waiting for services to create tables..."

# Function to seed a database
seed_db() {
  local db=$1
  local seed_file=$2
  local check_table=$3

  echo "Checking for table '$check_table' in database '$db'..."
  # Wait for the table to be created by the Spring Boot service
  until psql -h postgres-db -U irms_user -d "$db" -c "SELECT 1 FROM $check_table LIMIT 1;" > /dev/null 2>&1; do
    echo "Waiting for table '$check_table' in '$db'..."
    sleep 5
  done

  echo "Seeding database '$db' with '$seed_file'..."
  psql -h postgres-db -U irms_user -d "$db" < "/seeds/$seed_file"
}

# Seed each database
seed_db "irms_auth" "seed_auth.sql" "users"
seed_db "irms_menu" "seed_menu.sql" "menu_items"
seed_db "irms_table" "seed_table.sql" "restaurant_tables"
seed_db "irms_order" "seed_order.sql" "orders"
seed_db "irms_payment" "seed_payment.sql" "payments"
seed_db "irms_kitchen" "seed_kitchen.sql" "kitchen_tickets"

echo "Database seeding completed successfully!"
