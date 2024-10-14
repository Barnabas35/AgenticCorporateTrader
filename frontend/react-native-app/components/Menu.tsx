// src/components/Menu.tsx

import React from 'react';
import { NavLink } from 'react-router-dom';
import { View, Text, StyleSheet } from 'react-native';

const Menu: React.FC = () => {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>TradeAgently</Text>
      <View style={styles.menuContainer}>
        <NavLink to="/" style={({ isActive }) => (isActive ? styles.activeMenuItem : styles.menuItem)}>Home</NavLink>
        <View style={styles.spacer} />
        <NavLink to="/login-register" style={({ isActive }) => (isActive ? styles.activeMenuItem : styles.menuItem)}>Login/Register</NavLink>
        <View style={styles.spacer} />
        <NavLink to="/record-details" style={({ isActive }) => (isActive ? styles.activeMenuItem : styles.menuItem)}>Record Details</NavLink>
        {/* Add other menu items as needed */}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    height: '8%',
    padding: 10,
    backgroundColor: '#272727', // Background color of the menu
    flexDirection: 'row', // Align items vertically
    alignItems: 'center', // Center items horizontally
  },
  title: {
    fontSize: 39,
    color: 'white',
    marginBottom: 10,
    fontFamily: 'sans-serif', // Sans-serif font
    fontWeight: 'bold',
  },
  menuContainer: {
    flexDirection: 'row', // Align menu items horizontally
    justifyContent: 'center', // Center menu items
    width: '100%', // Full width to occupy the container
  },
  menuItem: {
    backgroundColor: '#288DFF', // Solid green background for menu items
    color: 'white', // Text color for menu items
    borderRadius: 20, // Rounded edges
    fontSize: 20,
    padding: 15, // Padding around the text
    textDecorationLine: 'none', // No underline for links
    fontWeight: 'bold',
    justifyContent: 'center'

    
  },
  activeMenuItem: {
    backgroundColor: '#94C4FA', // Light blue for active link
    fontSize: 20,
    color: 'white', // Black text for active link
    borderRadius: 20, // Keep rounded edges for active item
    padding: 15, // Padding around the text
    textDecorationLine: 'none', // No underline for active link
    fontWeight: 'bold',
    justifyContent: 'center'

  },
  spacer: {
    width: 15,
  }
});

export default Menu;
