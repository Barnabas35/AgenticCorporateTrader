import React, { useEffect, useState } from 'react';
import { NavLink } from 'react-router-dom';
import {
  View,
  Image,
  StyleSheet,
  Text,
  TouchableOpacity,
  ScrollView,
  Modal,
  Button,
} from 'react-native';
import { useUser } from './userContext';
import { FontAwesome } from '@expo/vector-icons';

const Menu: React.FC = () => {
  const { sessionToken } = useUser();
  const [userType, setUserType] = useState<string | null>(null);
  const [isMenuOpen, setIsMenuOpen] = useState<boolean>(false);
  const [isMobileView, setIsMobileView] = useState<boolean>(false);
  const [showMobileAppModal, setShowMobileAppModal] = useState<boolean>(false);
  const [balance, setBalance] = useState<number | null>(null);

  useEffect(() => {
    const fetchUserType = async () => {
      if (!sessionToken) {
        setUserType(null);
        return;
      }

      try {
        const response = await fetch('https://tradeagently.dev/get-user-type', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ session_token: sessionToken }),
        });

        const data = await response.json();
        if (data.status === 'Success') {
          setUserType(data.user_type);
        } else {
          console.error('Failed to fetch user type');
        }
      } catch (error) {
        console.error('Error fetching user type:', error);
      }
    };

    const fetchBalance = async () => {
      if (!sessionToken) {
        setBalance(null);
        return;
      }

      try {
        const response = await fetch('https://tradeagently.dev/get-balance', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ session_token: sessionToken }),
        });

        const data = await response.json();
        if (data.status === 'Success') {
          setBalance(data.balance);
        } else {
          console.error('Failed to fetch balance');
        }
      } catch (error) {
        console.error('Error fetching balance:', error);
      }
    };

    if (sessionToken) {
      fetchUserType();
      fetchBalance();

      const intervalId = setInterval(() => {
        fetchBalance();
      }, 10000); // Every 10 seconds

      return () => clearInterval(intervalId);
    } else {
      // If there's no session token, user is logged out, so clear user data
      setUserType(null);
      setBalance(null);
    }

    const handleResize = () => {
      const isMobile = window.innerWidth < 800;
      setIsMobileView(isMobile);
      if (isMobile) {
        setShowMobileAppModal(true);
      }
    };

    window.addEventListener('resize', handleResize);
    handleResize();

    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, [sessionToken]);

  return (
    <View style={[styles.container, isMenuOpen && styles.menuOpen]}>
      <View style={styles.navbar}>
        {/* Logo */}
        <NavLink to="/" style={styles.title}>
          <Image
            source={require('../assets/images/logo.png')}
            style={styles.logo}
          />
        </NavLink>

        {/* Display Balance only if logged in and balance is available */}
        {sessionToken && balance !== null && (
          <View style={styles.balanceContainer}>
            <Text style={styles.balanceText}>Balance: ${balance.toFixed(2)}</Text>
          </View>
        )}

        {/* Mobile View - Burger Menu */}
        {isMobileView ? (
          <TouchableOpacity
            onPress={() => setIsMenuOpen((prev) => !prev)}
            style={styles.burgerButton}
          >
            <FontAwesome name="bars" size={32} color="white" />
            {isMenuOpen && (
              <ScrollView style={styles.dropdownMenu}>
                {sessionToken ? (
                  <>
                    {userType === 'admin' && (
                      <NavLink
                        to="/admin"
                        style={({ isActive }) =>
                          isActive ? styles.activeDropdownItem : styles.dropdownItem
                        }
                      >
                        <FontAwesome name="wrench" size={16} color="white" /> Admin Tools
                      </NavLink>
                    )}
                    <View style={styles.spacer} />
                    {userType === 'fa' && (
                      <NavLink
                        to="/client-management"
                        style={({ isActive }) =>
                          isActive ? styles.activeDropdownItem : styles.dropdownItem
                        }
                      >
                        <FontAwesome name="users" size={16} color="white" /> Client Management
                      </NavLink>
                    )}
                    <View style={styles.spacer} />
                    <NavLink
                      to="/crypto-search"
                      style={({ isActive }) =>
                        isActive ? styles.activeDropdownItem : styles.dropdownItem
                      }
                    >
                      <FontAwesome name="bitcoin" size={16} color="white" /> Crypto Search
                    </NavLink>
                    <View style={styles.spacer} />
                    <NavLink
                      to="/stock-search"
                      style={({ isActive }) =>
                        isActive ? styles.activeDropdownItem : styles.dropdownItem
                      }
                    >
                      <FontAwesome name="line-chart" size={16} color="white" /> Stock Search
                    </NavLink>
                    <View style={styles.spacer} />
                    <NavLink
                      to="/user-account"
                      style={({ isActive }) =>
                        isActive ? styles.activeDropdownItem : styles.dropdownItem
                      }
                    >
                      <FontAwesome name="user" size={16} color="white" /> User Account
                    </NavLink>
                    <View style={styles.spacer} />
                  </>
                ) : (
                  <NavLink
                    to="/login-register"
                    style={({ isActive }) =>
                      isActive ? styles.activeDropdownItem : styles.dropdownItem
                    }
                  >
                    <FontAwesome name="sign-in" size={16} color="white" /> Login/Register
                  </NavLink>
                )}
              </ScrollView>
            )}
          </TouchableOpacity>
        ) : (
          /* Desktop View - Horizontal Menu */
          <View style={styles.menuContainer}>
            <NavLink
              to="/"
              style={({ isActive }) =>
                isActive ? styles.activeMenuItem : styles.menuItem
              }
            >
              <FontAwesome name="home" size={18} color="white" /> Home
            </NavLink>
              {!sessionToken && (
              <>
                <View style={styles.spacer} />
                <NavLink
                  to="/about"
                  style={({ isActive }) =>
                    isActive ? styles.activeMenuItem : styles.menuItem
                  }
                >
                  <FontAwesome name="info-circle" size={18} color="white" /> About
                </NavLink>
              </>
              )}

            <View style={styles.spacer} />
            {sessionToken ? (
              <>
                {userType === 'admin' && (
                  <NavLink
                    to="/admin"
                    style={({ isActive }) =>
                      isActive ? styles.activeMenuItem : styles.menuItem
                    }
                  >
                    <FontAwesome name="wrench" size={18} color="white" /> Admin Tools
                  </NavLink>
                )}
                {userType === 'fm' && (
                  <NavLink
                    to="/client-management"
                    style={({ isActive }) =>
                      isActive ? styles.activeMenuItem : styles.menuItem
                    }
                  >
                    <FontAwesome name="users" size={18} color="white" /> Client Management
                  </NavLink>
                )}
                {userType === 'fa' && (
                  <NavLink
                    to="/my-assets"
                    style={({ isActive }) =>
                      isActive ? styles.activeMenuItem : styles.menuItem
                    }
                  >
                    <FontAwesome name="list-alt" size={18} color="white" /> My Assets
                  </NavLink>
                )}
                <View style={styles.spacer} />
                <NavLink
                  to="/crypto-search"
                  style={({ isActive }) =>
                    isActive ? styles.activeMenuItem : styles.menuItem
                  }
                >
                  <FontAwesome name="bitcoin" size={18} color="white" /> Crypto Search
                </NavLink>
                <View style={styles.spacer} />
                <NavLink
                  to="/stock-search"
                  style={({ isActive }) =>
                    isActive ? styles.activeMenuItem : styles.menuItem
                  }
                >
                  <FontAwesome name="line-chart" size={18} color="white" /> Stock Search
                </NavLink>
                <View style={styles.spacer} />
                <NavLink
                  to="/user-account"
                  style={({ isActive }) =>
                    isActive ? styles.activeMenuItem : styles.menuItem
                  }
                >
                  <FontAwesome name="user" size={18} color="white" /> User Account
                </NavLink>
              </>
            ) : (
              <NavLink
                to="/login"
                style={({ isActive }) =>
                  isActive ? styles.activeMenuItem : styles.menuItem
                }
              >
                <FontAwesome name="sign-in" size={18} color="white" /> Login/Register
              </NavLink>
            )}
          </View>
        )}
      </View>

      {/* Mobile App Modal */}
      {isMobileView && (
        <Modal
          animationType="slide"
          transparent={true}
          visible={showMobileAppModal}
          onRequestClose={() => {
            setShowMobileAppModal(false);
          }}
        >
          <View style={styles.modalOverlay}>
            <View style={styles.modalContent}>
              <Text style={styles.modalText}>
                For a better experience, we recommend using our mobile app on
                your mobile device.
              </Text>
              <Button
                title="OK"
                onPress={() => setShowMobileAppModal(false)}
              />
            </View>
          </View>
        </Modal>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#282424',
    height: 105,
    flexDirection: 'column',
    justifyContent: 'flex-start',
    zIndex: 1,
  },
  navbar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
  },
  menuOpen: {
    height: 400,
  },
  title: {
    textDecorationLine: 'none',
  },
  logo: {
    width: 400,
    height: 120,
    resizeMode: 'contain',
  },
  menuContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    position: 'absolute',
    left: '55%',
    transform: [{ translateX: '-50%' }],
    width: '100%',
  },
  menuItem: {
    backgroundColor: '#4CAF50',
    color: 'white',
    borderRadius: 10,
    fontSize: 20,
    letterSpacing: 2,
    fontFamily: 'sans-serif',
    padding: 15,
    textDecorationLine: 'none',
    fontWeight: 'bold',
    marginHorizontal: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 5,
  },
  activeMenuItem: {
    backgroundColor: '#E85759',
    fontSize: 20,
    color: 'white',
    borderRadius: 10,
    padding: 15,
    letterSpacing: 2,
    fontFamily: 'sans-serif',
    textDecorationLine: 'none',
    fontWeight: 'bold',
    marginHorizontal: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 5,
  },
  dropdownItem: {
    padding: 15,
    backgroundColor: '#4CAF50',
    borderRadius: 10,
    marginVertical: 5,
    color: 'white',
    fontWeight: 'bold',
    textDecorationLine: 'none',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 5,
  },
  activeDropdownItem: {
    padding: 15,
    backgroundColor: '#E85759',
    borderRadius: 10,
    marginVertical: 5,
    color: 'white',
    fontWeight: 'bold',
    textDecorationLine: 'none',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 5,
  },
  spacer: {
    width: 15,
  },
  burgerButton: {
    padding: 10,
  },
  dropdownMenu: {
    position: 'absolute',
    top: 40,
    right: 20,
    backgroundColor: '#272727',
    padding: 10,
    borderRadius: 10,
    width: 200,
    zIndex: 1000,
  },
  modalOverlay: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
  },
  modalContent: {
    backgroundColor: 'white',
    padding: 20,
    borderRadius: 10,
    alignItems: 'center',
    width: 300,
  },
  modalText: {
    fontSize: 16,
    marginBottom: 10,
    textAlign: 'center',
  },
  balanceContainer: {
    backgroundColor: '#4CAF50',
    paddingVertical: 8,
    paddingHorizontal: 15,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
    marginLeft: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 5,
  },
  balanceText: {
    fontSize: 16,
    color: 'white',
    fontWeight: 'bold',
  },
});

export default Menu;
