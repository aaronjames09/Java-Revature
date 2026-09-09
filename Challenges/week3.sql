SELECT * FROM customer;

SELECT * FROM customer WHERE state = 'AZ';

SELECT * FROM invoice WHERE invoice_date < CURRENT_DATE - INTERVAL '6 months';

UPDATE customer SET phone = NULL where phone !~ '^\+1 [0-9]{3} [0-9]{3}-[0-9]{4}';

SELECT * FROM track WHERE milliseconds>180000;

UPDATE customer SET country = 'USA', address = NULL, city = NULL, state = NULL WHERE country != 'USA';

CREATE OR REPLACE FUNCTION total_spend(customer_id INT)
RETURNS INT AS $$
BEGIN
    RETURN (SELECT SUM(total) FROM invoice WHERE invoice.customer_id = total_spend.customer_id);

END;
$$ LANGUAGE plpgsql;

CREATE PROCEDURE update_reports(p_employee_id INT, manager_employee_id INT)
LANGUAGE plpgsql
AS $$
DECLARE 
    current_manager INT;
BEGIN
    IF p_employee_id = manager_employee_id THEN
        RAISE EXCEPTION 'An employee can not report to themself.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM employee WHERE employee_id = manager_employee_id
    )
    THEN RAISE EXCEPTION 'Manager does not exist';
    END IF;

    current_manager := manager_employee_id;
    WHILE current_manager IS NOT NULL LOOP
      IF current_manager = p_employee_id THEN
            RAISE EXCEPTION 'This creates a circular management relationship';
        END IF;

        SELECT reports_to INTO current_manager FROM employee WHERE employee_id = current_manager;

    END LOOP;

    UPDATE employee SET reports_to = manager_employee_id WHERE employee_id = p_employee_id;


END;
$$;

CALL update_reports(5,1);


CREATE SCHEMA pets;
CREATE TABLE customer (
    customer_id INT PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(20)
);
CREATE TABLE pet (
    pet_id INT PRIMARY KEY,
    pet_name VARCHAR(50),
    species VARCHAR(30),
    breed VARCHAR(50),
    customer_id INT,
    FOREIGN KEY (customer_id)
        REFERENCES pets.customer(customer_id)
);
INSERT INTO pets.customer (customer_id, first_name, last_name, phone)
VALUES
    (1, 'John', 'Smith', '+1 555 555-5555'),
    (2, 'Sarah', 'Jones', '+1 555 123-4567'),
    (3, 'Mike', 'Brown', '+1 555 987-6543');
INSERT INTO pets.pet (pet_id, pet_name, species, breed, customer_id)
VALUES
    (1, 'Buddy', 'Dog', 'Golden Retriever', 1),
    (2, 'Mittens', 'Cat', 'Siamese', 1),
    (3, 'Max', 'Dog', 'Labrador', 2),
    (4, 'Luna', 'Cat', 'Persian', 3);