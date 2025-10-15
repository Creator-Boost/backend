#!/bin/bash
echo "Starting Payment Service in Development Mode..."
echo ""
echo "Make sure to set your STRIPE_SECRET_KEY environment variable:"
echo "export STRIPE_SECRET_KEY=sk_test_your_key_here"
echo ""
echo "Or create a .env file with your configuration."
echo ""
read -p "Press Enter to continue..."

mvn spring-boot:run -Dspring-boot.run.profiles=dev
