// App.tsx or App.jsx
import React, { useEffect } from 'react';
import { Elements } from '@stripe/react-stripe-js';
import { loadStripe } from '@stripe/stripe-js';
import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom';
import { View, Text, StyleSheet } from 'react-native';
import Menu from './components/Menu';
import Home from './app/index';
import Login from './app/login';
import Contact from './app/contact';
import Register from './app/register';
import UserAccount from './app/userAccount';
import CryptoSearch from './app/cryptoSearch';
import StockSearch from './app/stock-search';
import About from './app/about';
import ClientManagement from './app/client-management';
import Admin from './app/admin';
import MyAssets from './app/my-assets';
import CheckoutForm from './components/CheckoutForm';
import CompletePage from './components/CompletePage';
import { Helmet } from 'react-helmet';

// Initialize Stripe
const stripePromise = loadStripe('pk_test_51QP2IwFp664itGdOwg1hyEpDcLxfaD29psic6hcZ5lnmO6MUZNXnu0Vft1kZk8pLx4BGc6ofKD9oZS4pHPdBj5tz00lLw5IBU5');

const App: React.FC = () => {

  return (
    <Router>
      <Helmet>
        <title>Tradeagently</title>
        <link rel="icon" href="./assets/images/logo1.png" />
      </Helmet>
      <View style={styles.container}>
        <Menu />
        <View style={styles.content}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/contact" element={<Contact />} />
            <Route path="/register" element={<Register />} />
            <Route path="/user-account" element={<UserAccount />} />
            <Route path="/about" element={<About />} />
            <Route path="/client-management" element={<ClientManagement />} />
            <Route path="/crypto-search" element={<CryptoSearch />} />
            <Route path="/stock-search" element={<StockSearch />} />
            <Route path="/admin" element={<Admin />} />
            <Route path="/my-assets" element={<MyAssets />} />
            <Route path="/checkout" element={<CheckoutWithElements />} />
            <Route path="/complete" element={<CompleteWithElements />} />
          </Routes>
        </View>
      </View>
    </Router>
  );
};

// Checkout route wrapper to pass clientSecret dynamically
const CheckoutWithElements: React.FC = () => {
  const location = useLocation();
  const { clientSecret } = location.state || {}; // Retrieve clientSecret from navigation state

  if (!clientSecret) {
    return (
      <View style={styles.errorContainer}>
        <Text style={styles.errorText}>Error: Missing payment details.</Text>
      </View>
    );
  }

  return (
    <Elements stripe={stripePromise} options={{ clientSecret }}>
      <CheckoutForm />
    </Elements>
  );
};

// CompletePage wrapper to pass clientSecret dynamically from URL
const CompleteWithElements: React.FC = () => {
  const location = useLocation();
  const clientSecret = new URLSearchParams(location.search).get('payment_intent_client_secret');

  if (!clientSecret) {
    return (
      <View style={styles.errorContainer}>
        <Text style={styles.errorText}>Error: Missing payment details.</Text>
      </View>
    );
  }

  return (
    <Elements stripe={stripePromise} options={{ clientSecret }}>
      <CompletePage />
    </Elements>
  );
};

// Define styles using StyleSheet
const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f0f0f0',
    flexDirection: 'column',
  },
  content: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'flex-start',
    backgroundColor: '#fff',
  },
  errorContainer: {
    flex: 1,
    alignItems: 'center',
    backgroundColor: '#f0f0f0',
    justifyContent: 'center',
    padding: 20,
  },
  errorText: {
    color: '#e74c3c',
    fontSize: 18,
    textAlign: 'center',
  },
});

export default App;
