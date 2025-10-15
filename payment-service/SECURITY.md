# Payment Service Security Guide

## Environment Variables Setup

### Required Environment Variables

1. **STRIPE_SECRET_KEY** - Your Stripe secret key
   - Get from: https://dashboard.stripe.com/apikeys
   - Format: `sk_test_...` (test) or `sk_live_...` (production)

### Setting Environment Variables

#### Windows (Command Prompt)

```cmd
set STRIPE_SECRET_KEY=sk_test_your_key_here
```

#### Windows (PowerShell)

```powershell
$env:STRIPE_SECRET_KEY="sk_test_your_key_here"
```

#### Linux/Mac

```bash
export STRIPE_SECRET_KEY=sk_test_your_key_here
```

#### Docker

```bash
docker run -e STRIPE_SECRET_KEY=sk_test_your_key_here your-image
```

#### Docker Compose

```yaml
environment:
  - STRIPE_SECRET_KEY=sk_test_your_key_here
```

### Security Best Practices

1. **Never commit secret keys to version control**
2. **Use different keys for different environments**
3. **Rotate keys regularly**
4. **Use environment variables in production**
5. **Monitor key usage in Stripe dashboard**

### Development Setup

1. Copy `env.example` to `.env`
2. Fill in your Stripe test key
3. Run the application

### Production Setup

1. Set `STRIPE_SECRET_KEY` environment variable
2. Use production Stripe keys (`sk_live_...`)
3. Ensure proper access controls

### Troubleshooting

If you see "Stripe secret key not configured" error:

1. Check if `STRIPE_SECRET_KEY` is set
2. Verify the key format (starts with `sk_`)
3. Ensure no extra spaces or quotes
