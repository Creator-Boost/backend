# Gig Service API Testing Guide

## Prerequisites
1. Start the gig service: `.\mvnw.cmd spring-boot:run`
2. Service should be running on http://localhost:8080

## Test Scenarios

### 1. Create a Gig with Packages
**Endpoint:** POST http://localhost:8080/api/gigs
**Content-Type:** application/json

**Request Body:**
```json
{
  "sellerId": "123e4567-e89b-12d3-a456-426614174000",
  "title": "Professional Logo Design Service",
  "description": "I will create a stunning, professional logo for your business that captures your brand identity and stands out from the competition.",
  "platform": "Instagram",
  "category": "Graphic Design",
  "status": "ACTIVE",
  "images": [
    {
      "url": "https://example.com/logo-portfolio-1.jpg",
      "isPrimary": true
    },
    {
      "url": "https://example.com/logo-portfolio-2.jpg",
      "isPrimary": false
    }
  ],
  "packages": [
    {
      "name": "Basic Logo",
      "price": 25.00,
      "deliveryDays": 2,
      "description": "1 logo concept, 2 revisions, PNG & JPG files"
    },
    {
      "name": "Standard Logo",
      "price": 50.00,
      "deliveryDays": 3,
      "description": "3 logo concepts, 5 revisions, PNG, JPG & SVG files, social media kit"
    },
    {
      "name": "Premium Logo",
      "price": 100.00,
      "deliveryDays": 5,
      "description": "5 logo concepts, unlimited revisions, all file formats, brand guidelines, business card design"
    }
  ],
  "faqs": [
    {
      "question": "What file formats will I receive?",
      "answer": "You'll receive PNG, JPG, and SVG formats for the Standard and Premium packages."
    },
    {
      "question": "How many revisions are included?",
      "answer": "Basic includes 2 revisions, Standard includes 5 revisions, and Premium includes unlimited revisions."
    }
  ]
}
```

### 2. Get Gig with Specific Package Details
**Endpoint:** GET http://localhost:8080/api/gigs/{gigId}/packages/{packageId}

**Example:** GET http://localhost:8080/api/gigs/987fcdeb-51a2-43d1-b123-426614174001/packages/abc12345-6789-1011-1213-141516171819

**Expected Response:**
```json
{
  "gigId": "987fcdeb-51a2-43d1-b123-426614174001",
  "sellerId": "123e4567-e89b-12d3-a456-426614174000",
  "title": "Professional Logo Design Service",
  "description": "I will create a stunning, professional logo for your business...",
  "platform": "Instagram",
  "category": "Graphic Design",
  "status": "ACTIVE",
  "createdAt": "2025-08-03T14:30:00",
  "updatedAt": "2025-08-03T14:30:00",
  "images": [
    {
      "url": "https://example.com/logo-portfolio-1.jpg",
      "isPrimary": true
    }
  ],
  "faqs": [
    {
      "question": "What file formats will I receive?",
      "answer": "You'll receive PNG, JPG, and SVG formats..."
    }
  ],
  "allPackages": [
    {
      "name": "Basic Logo",
      "price": 25.00,
      "deliveryDays": 2,
      "description": "1 logo concept, 2 revisions, PNG & JPG files"
    },
    {
      "name": "Standard Logo",
      "price": 50.00,
      "deliveryDays": 3,
      "description": "3 logo concepts, 5 revisions, PNG, JPG & SVG files, social media kit"
    }
  ],
  "selectedPackage": {
    "packageId": "abc12345-6789-1011-1213-141516171819",
    "gigId": "987fcdeb-51a2-43d1-b123-426614174001",
    "name": "Standard Logo",
    "price": 50.00,
    "deliveryDays": 3,
    "description": "3 logo concepts, 5 revisions, PNG, JPG & SVG files, social media kit"
  }
}
```

### 3. Verify Gig and Package Existence
**Endpoint:** GET http://localhost:8080/api/gigs/{gigId}/packages/{packageId}/verify

**Example:** GET http://localhost:8080/api/gigs/987fcdeb-51a2-43d1-b123-426614174001/packages/abc12345-6789-1011-1213-141516171819/verify

**Expected Response:**
```json
{
  "exists": true
}
```

### 4. Get All Packages for a Gig
**Endpoint:** GET http://localhost:8080/api/gigs/{gigId}/packages

**Example:** GET http://localhost:8080/api/gigs/987fcdeb-51a2-43d1-b123-426614174001/packages

**Expected Response:**
```json
[
  {
    "packageId": "abc12345-6789-1011-1213-141516171819",
    "gigId": "987fcdeb-51a2-43d1-b123-426614174001",
    "name": "Basic Logo",
    "price": 25.00,
    "deliveryDays": 2,
    "description": "1 logo concept, 2 revisions, PNG & JPG files"
  },
  {
    "packageId": "def67890-1234-5678-9012-345678901234",
    "gigId": "987fcdeb-51a2-43d1-b123-426614174001",
    "name": "Standard Logo",
    "price": 50.00,
    "deliveryDays": 3,
    "description": "3 logo concepts, 5 revisions, PNG, JPG & SVG files, social media kit"
  },
  {
    "packageId": "ghi12345-6789-0123-4567-890123456789",
    "gigId": "987fcdeb-51a2-43d1-b123-426614174001",
    "name": "Premium Logo",
    "price": 100.00,
    "deliveryDays": 5,
    "description": "5 logo concepts, unlimited revisions, all file formats, brand guidelines, business card design"
  }
]
```

### 5. Error Cases to Test

#### Invalid Gig ID:
**Endpoint:** GET http://localhost:8080/api/gigs/invalid-id/packages/any-package-id
**Expected Response:** 404 Not Found

#### Invalid Package ID:
**Endpoint:** GET http://localhost:8080/api/gigs/valid-gig-id/packages/invalid-package-id
**Expected Response:** 404 Not Found

#### Package doesn't belong to Gig:
**Endpoint:** GET http://localhost:8080/api/gigs/gig-id-1/packages/package-from-gig-id-2
**Expected Response:** 404 Not Found

## Testing with cURL Commands

### Create Gig:
```bash
curl -X POST http://localhost:8080/api/gigs \
  -H "Content-Type: application/json" \
  -d @create-gig.json
```

### Get Gig with Package:
```bash
curl -X GET "http://localhost:8080/api/gigs/{gigId}/packages/{packageId}"
```

### Verify Existence:
```bash
curl -X GET "http://localhost:8080/api/gigs/{gigId}/packages/{packageId}/verify"
```

### Get All Packages:
```bash
curl -X GET "http://localhost:8080/api/gigs/{gigId}/packages"
```

## Testing with Postman

1. Import the endpoints into Postman
2. Set up environment variables:
   - `baseUrl`: http://localhost:8080
   - `gigId`: (use the ID from create response)
   - `packageId`: (use a package ID from create response)

3. Create a test collection with the following requests:
   - POST {{baseUrl}}/api/gigs
   - GET {{baseUrl}}/api/gigs/{{gigId}}/packages/{{packageId}}
   - GET {{baseUrl}}/api/gigs/{{gigId}}/packages/{{packageId}}/verify
   - GET {{baseUrl}}/api/gigs/{{gigId}}/packages

## Testing Steps:

1. **Start the service:** `.\mvnw.cmd spring-boot:run`
2. **Create a gig** using the POST endpoint and note the `gigId` and package IDs from the response
3. **Test the new endpoints** using the actual IDs from step 2
4. **Verify validation** by testing with invalid IDs
