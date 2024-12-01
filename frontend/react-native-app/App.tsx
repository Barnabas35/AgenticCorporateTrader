// src/App.tsx

import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Menu from './components/Menu'; // Ensure correct import path
import Home from './app/index';
import Login from './app/login';
import Contact from './app/contact';
import Register from './app/register';
import UserAccount from './app/userAccount';
import CryptoSearch from './app/cryptoSearch';
import StockSearch from './app/stock-search';
import About from './app/about'
import ClientManagement from './app/client-management';
import Admin from './app/admin';
import MyAssets from './app/my-assets';
import { View, StyleSheet } from 'react-native'; // Import View and StyleSheet for layout
import { UserProvider } from './components/userContext';

const App: React.FC = () => {
  return (
    <UserProvider>
      <Router>
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
              <Route path="/my-assets" element={< MyAssets />} />
            </Routes>
          </View>
        </View>
      </Router>
    </UserProvider>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff', // Background color of the entire app
  },
  content: {
    flex: 1,
    alignItems: 'center', // Center align items if you want them centered
    justifyContent: 'flex-start', // Start from the top
  },
});

export default App;
