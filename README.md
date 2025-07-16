# E-Commerce Application with Spring Cloud OpenFeign Bank Integration

This project demonstrates a complete e-commerce application integrated with a banking system using Spring Cloud OpenFeign for microservices communication.

## Architecture Overview

- **EcommerceApp** (Port 8081): Main e-commerce application
- **BankApp** (Port 8080): Banking service for payment processing
- **Integration**: OpenFeign client for seamless communication

## Features Implemented

### E-Commerce Application Features

1. **User Registration & Login**
   - User registration with bank account linking
   - Authentication system
   - User profile management

2. **Product Management**
   - Product catalog with categories
   - Search functionality (works with/without login)
   - Stock management

3. **Shopping Cart**
   - Add/remove products
   - Quantity management
   - Cart total calculation

4. **Purchase System**
   - Order creation with bank verification
   - Payment processing via OpenFeign
   - Order status tracking

5. **Dashboard Analytics**
   - Monthly order reports
   - Date range filtering
   - Purchase history

### Bank Integration Features

1. **Account Verification**
   - Verify customer bank account exists
   - Real-time account validation

2. **Payment Processing**
   - Check sufficient funds before purchase
   - Transfer funds from customer to e-commerce account
   - Transaction ID tracking

## OpenFeign Integration

### BankServiceClient
```java
@FeignClient(name = "bank-service", url = "${bank.service.url}")
public interface BankServiceClient {
    @GetMapping("/api/accounts/{accountNumber}")
    ResponseEntity<Account> getAccountByNumber(@PathVariable String accountNumber);
    
    @PostMapping("/api/transfers")
    ResponseEntity<Transaction> transferFunds(@RequestBody TransferRequest request);
}
```

## API Endpoints

### User Management
- `POST /api/users/register` - Register new user
- `POST /api/users/login` - User login
- `GET /api/users/{id}` - Get user details

### Product Management
- `GET /api/products` - Get all products
- `GET /api/products/search?keyword={keyword}` - Search products
- `GET /api/products/category/{category}` - Get products by category

### Shopping Cart
- `POST /api/cart/add` - Add product to cart
- `GET /api/cart/user/{userId}` - Get cart items
- `DELETE /api/cart/remove/{cartItemId}` - Remove from cart

### Order Management
- `POST /api/orders/purchase/{userId}` - Purchase cart items
- `GET /api/orders/user/{userId}` - Get user orders
- `GET /api/orders/dashboard/{userId}` - Get monthly dashboard

### Demo Endpoints
- `GET /api/demo/complete-flow` - Demonstrate full e-commerce flow
- `GET /api/demo/features-overview` - List all features
- `GET /api/demo/test-bank-connectivity` - Test bank integration

## Setup Instructions

### 1. Start BankApp (Port 8080)
```bash
cd BankApp
mvn spring-boot:run
```

### 2. Start EcommerceApp (Port 8081)
```bash
cd ecommerceapp
mvn spring-boot:run
```

### 3. Test the Integration

#### Complete Demo Flow
```bash
curl http://localhost:8081/api/demo/complete-flow
```

#### Test Bank Connectivity
```bash
curl http://localhost:8081/api/demo/test-bank-connectivity
```

## Sample Data

### Pre-created Bank Accounts
- **ECM001**: E-commerce company account (receives payments)
- **CUST001**: John Doe - $1000.00 balance
- **CUST002**: Jane Smith - $750.00 balance

### Sample Products
- Classic Denim Jacket - $79.99
- Cotton T-Shirt - $24.99
- Skinny Jeans - $59.99
- Summer Dress - $89.99
- Sneakers - $99.99

## Testing the Complete Flow

1. **Register User**: Create user with bank account CUST001
2. **Login**: Authenticate user
3. **Search Products**: Find products by keyword
4. **Add to Cart**: Add selected products
5. **Verify Bank Account**: Check account exists and has funds
6. **Purchase**: Process payment via OpenFeign integration
7. **Dashboard**: View purchase history

## Error Handling

- Bank service connectivity issues
- Insufficient funds validation
- Stock availability checks
- Invalid account number handling

## Technology Stack

- Spring Boot 3.5.3
- Spring Cloud OpenFeign
- Spring Data JPA
- H2 Database
- Lombok
- Maven

## Database Configuration

Both applications use H2 in-memory databases:
- **BankApp**: jdbc:h2:mem:test
- **EcommerceApp**: jdbc:h2:mem:ecommerce

Access H2 consoles:
- BankApp: http://localhost:8080/h2-console
- EcommerceApp: http://localhost:8081/h2-console
