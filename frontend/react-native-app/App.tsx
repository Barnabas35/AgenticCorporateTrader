// src/App.tsx

import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Menu from './components/Menu'; // Ensure correct import path
import Home from './app/home';
import Login from './app/login';
import Contact from './app/contact';
import { View, StyleSheet } from 'react-native'; // Import View and StyleSheet for layout

const App: React.FC = () => {
  return (
    <Router>
      <View style={styles.container}>
        <Menu />
        <View style={styles.content}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login-register" element={<Login />} />
            <Route path="/contact" element={<Contact />} />
          </Routes>
        </View>
      </View>
    </Router>
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
