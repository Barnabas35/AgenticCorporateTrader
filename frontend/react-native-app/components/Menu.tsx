import React, { useEffect, useState } from 'react';
import { NavLink } from 'react-router-dom';
import { View, Image, StyleSheet } from 'react-native';
import { useUser } from './userContext'; // Import the user context

const Menu: React.FC = () => {
  const { sessionToken } = useUser(); // Access sessionToken from userContext
  const [userType, setUserType] = useState<string | null>(null);

  useEffect(() => {
    const fetchUserType = async () => {
      try {
        const response = await fetch('https://tradeagently.dev/get-user-type', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ session_token: sessionToken }),
        });

        const data = await response.json();
        if (data.status === 'Success') {
          setUserType(data.user_type); // Set user type if request is successful
          console.log(data.user_type);
        } else {
          console.error('Failed to fetch user type');
        }
      } catch (error) {
        console.error('Error fetching user type:', error);
      }
    };

    if (sessionToken) {
      fetchUserType();
    }
  }, [sessionToken]);

  return (
    <View style={styles.container}>
      {/* Left-aligned logo */}
      <NavLink to="/" style={styles.title}>
        <Image
          source={require('../assets/images/logo.png')} // Replace with your logo path
          style={styles.logo}
        />
      </NavLink>

      {/* Centered menu items */}
      <View style={styles.menuContainer}>
        <NavLink
          to="/"
          style={({ isActive }) => (isActive ? styles.activeMenuItem : styles.menuItem)}
        >
          Home
        </NavLink>
        <View style={styles.spacer} />
        <NavLink
          to="/about"
          style={({ isActive }) => (isActive ? styles.activeMenuItem : styles.menuItem)}
        >
          About
        </NavLink>
        <View style={styles.spacer} />
        {sessionToken ? (
          <>
            {/* Conditionally render Client Management for 'fa' user type only */}
            {userType === 'fa' && (
              <>
                <NavLink
                  to="/client-management"
                  style={({ isActive }) => (isActive ? styles.activeMenuItem : styles.menuItem)}
                >
                  Client Management
                </NavLink>
                <View style={styles.spacer} />
              </>
            )}
            <NavLink
              to="/crypto-search"
              style={({ isActive }) => (isActive ? styles.activeMenuItem : styles.menuItem)}
            >
              Crypto Search
            </NavLink>
            <View style={styles.spacer} />
            <NavLink
              to="/stock-search"
              style={({ isActive }) => (isActive ? styles.activeMenuItem : styles.menuItem)}
            >
              Stock Search
            </NavLink>
            <View style={styles.spacer} />
            <NavLink
              to="/user-account"
              style={({ isActive }) => (isActive ? styles.activeMenuItem : styles.menuItem)}
            >
              User Account
            </NavLink>
          </>
        ) : (
          <NavLink
            to="/login-register"
            style={({ isActive }) => (isActive ? styles.activeMenuItem : styles.menuItem)}
          >
            Login/Register
          </NavLink>
        )}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    height: '8%',
    padding: 10,
    backgroundColor: '#272727', // Background color of the menu
    flexDirection: 'row', // Align items horizontally
    alignItems: 'center', // Center items vertically
    justifyContent: 'space-between', // Space between title and menu
    paddingHorizontal: 20, // Add horizontal padding to center content
  },
  title: {
    textDecorationLine: 'none', // No underline for the link
  },
  logo: {
    width: 350, // Adjust the size of the logo as needed
    height: 120,
    resizeMode: 'contain', // Ensures the logo maintains aspect ratio
  },
  menuContainer: {
    flexDirection: 'row', // Align menu items horizontally
    alignItems: 'center', // Center vertically
    justifyContent: 'center', // Center horizontally
    position: 'absolute', // Position it absolute to center on the screen
    left: '50%', // Position it in the center of the screen
    transform: [{ translateX: '-50%' }], // Adjust for proper centering
  },
  menuItem: {
    backgroundColor: '#4CAF50', // Solid green background for menu items
    color: 'white', // Text color for menu items
    borderRadius: 20, // Rounded edges
    fontSize: 20,
    fontFamily: 'sans-serif',
    padding: 15, // Padding around the text
    textDecorationLine: 'none', // No underline for links
    fontWeight: 'bold',
  },
  activeMenuItem: {
    backgroundColor: '#E85759', // Light red for active link
    fontSize: 20,
    color: 'white', // White text for active link
    borderRadius: 20, // Keep rounded edges for active item
    padding: 15, // Padding around the text
    fontFamily: 'sans-serif',
    textDecorationLine: 'none', // No underline for active link
    fontWeight: 'bold',
  },
  spacer: {
    width: 15,
  },
});

export default Menu;
