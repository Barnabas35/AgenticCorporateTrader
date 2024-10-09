import React, { useState, useRef, useEffect } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, SafeAreaView, ScrollView, Animated, Easing } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';


const Stack = createStackNavigator();

// Home Screen Component
function HomeScreen({ navigation }) {
  const [menuOpen, setMenuOpen] = useState(false);
  
  const animatedValue1 = useRef(new Animated.Value(0)).current;
  const animatedValue2 = useRef(new Animated.Value(0)).current;
  const animatedValue3 = useRef(new Animated.Value(0)).current;
  const animatedValue4 = useRef(new Animated.Value(0)).current;
  const animatedValue5 = useRef(new Animated.Value(0)).current;
  const animatedValue6 = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    if (menuOpen) {
      Animated.stagger(150, [
        Animated.timing(animatedValue1, {
          toValue: 1,
          duration: 300,
          useNativeDriver: true,
          easing: Easing.ease,
        }),
        Animated.timing(animatedValue2, {
          toValue: 1,
          duration: 300,
          useNativeDriver: true,
          easing: Easing.ease,
        }),
        Animated.timing(animatedValue3, {
          toValue: 1,
          duration: 300,
          useNativeDriver: true,
          easing: Easing.ease,
        }),
        Animated.timing(animatedValue4, {
          toValue: 1,
          duration: 300,
          useNativeDriver: true,
          easing: Easing.ease,
        }),
        Animated.timing(animatedValue5, {
          toValue: 1,
          duration: 300,
          useNativeDriver: true,
          easing: Easing.ease,
        }),
        Animated.timing(animatedValue6, {
          toValue: 1,
          duration: 300,
          useNativeDriver: true,
          easing: Easing.ease,
        }),
      ]).start();
    } else {
      [animatedValue1, animatedValue2, animatedValue3, animatedValue4, animatedValue5, animatedValue6].forEach((anim) => {
        Animated.timing(anim, {
          toValue: 0,
          duration: 300,
          useNativeDriver: true,
          easing: Easing.ease,
        }).start();
      });
    }
  }, [menuOpen]);

  const toggleMenu = () => {
    setMenuOpen(!menuOpen);
  };

  const getMenuStyle = (animatedValue: Animated.Value) => ({
    opacity: animatedValue,
    transform: [{ translateY: animatedValue.interpolate({ inputRange: [0, 1], outputRange: [-20, 0] }) }],
  });

  return (
    <SafeAreaView style={styles.safeArea}>
      {/* Top Bar with Hamburger */}
      <View style={styles.topBar}>
        <TouchableOpacity onPress={toggleMenu}>
          <Ionicons name="menu" size={32} color="white" />
        </TouchableOpacity>
        <Text style={styles.topBarTitle}>Investment Fund App</Text>
      </View>

      {/* Dropdown Menu */}
      {menuOpen && (
        <View style={styles.menuContainer}>
          <Animated.View style={[styles.menuItem, getMenuStyle(animatedValue1)]}>
            <TouchableOpacity onPress={() => navigation.navigate('Registration')}>
              <Text style={styles.menuItemText}>Registration / Login of fund managers</Text>
            </TouchableOpacity>
          </Animated.View>
          <Animated.View style={[styles.menuItem, getMenuStyle(animatedValue2)]}>
            <TouchableOpacity onPress={() => navigation.navigate('Details')}>
              <Text style={styles.menuItemText}>Record details of the fund manager</Text>
            </TouchableOpacity>
          </Animated.View>
          <Animated.View style={[styles.menuItem, getMenuStyle(animatedValue3)]}>
            <TouchableOpacity onPress={() => navigation.navigate('Access')}>
              <Text style={styles.menuItemText}>Access Restrictions</Text>
            </TouchableOpacity>
          </Animated.View>
          <Animated.View style={[styles.menuItem, getMenuStyle(animatedValue4)]}>
            <TouchableOpacity onPress={() => navigation.navigate('AI')}>
              <Text style={styles.menuItemText}>ACT-AI Engine</Text>
            </TouchableOpacity>
          </Animated.View>
          <Animated.View style={[styles.menuItem, getMenuStyle(animatedValue5)]}>
            <TouchableOpacity onPress={() => navigation.navigate('Reports')}>
              <Text style={styles.menuItemText}>Reports</Text>
            </TouchableOpacity>
          </Animated.View>
          <Animated.View style={[styles.menuItem, getMenuStyle(animatedValue6)]}>
            <TouchableOpacity onPress={() => navigation.navigate('Price Alerts')}>
              <Text style={styles.menuItemText}>Price Alerts</Text>
            </TouchableOpacity>
          </Animated.View>
        </View>
      )}

      {/* Centered Content Area */}
      <View style={styles.centeredContent}>
        <Text style={styles.contentText}>
          Welcome to the Investment Fund App! Use the menu to explore options.
        </Text>
      </View>
    </SafeAreaView>
  );
}

// Other Pages
const RegistrationScreen = () => (
  <View style={styles.centeredContent}>
    <Text style={styles.contentText}>Registration/Login Page</Text>
  </View>
);

const DetailsScreen = () => (
  <View style={styles.centeredContent}>
    <Text style={styles.contentText}>Fund Manager Details Page</Text>
  </View>
);

const AccessScreen = () => (
  <View style={styles.centeredContent}>
    <Text style={styles.contentText}>Access Restrictions Page</Text>
  </View>
);

const AIScreen = () => (
  <View style={styles.centeredContent}>
    <Text style={styles.contentText}>ACT-AI Engine Page</Text>
  </View>
);

const ReportsScreen = () => (
  <View style={styles.centeredContent}>
    <Text style={styles.contentText}>Reports Page</Text>
  </View>
);

const PriceAlertsScreen = () => (
  <View style={styles.centeredContent}>
    <Text style={styles.contentText}>Price Alerts Page</Text>
  </View>
);

// Main App with Navigation
export default function MainApp() {
  return (
    <NavigationContainer>
      <Stack.Navigator initialRouteName="Home">
        <Stack.Screen name="Home" component={HomeScreen} />
        <Stack.Screen name="Registration" component={RegistrationScreen} />
        <Stack.Screen name="Details" component={DetailsScreen} />
        <Stack.Screen name="Access" component={AccessScreen} />
        <Stack.Screen name="AI" component={AIScreen} />
        <Stack.Screen name="Reports" component={ReportsScreen} />
        <Stack.Screen name="Price Alerts" component={PriceAlertsScreen} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#94bf73',
  },
  topBar: {
    height: 60,
    backgroundColor: '#11150d',
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
  },
  topBarTitle: {
    color: 'white',
    fontSize: 20,
    fontWeight: 'bold',
    marginLeft: 16,
  },
  menuContainer: {
    position: 'absolute',
    top: 60,
    left: 0,
    width: '40%',
    backgroundColor: 'black',
    padding: 10,
  },
  menuItem: {
    paddingVertical: 10,
    borderBottomColor: 'white',
    borderBottomWidth: 1,
  },
  menuItemText: {
    color: 'white',
    fontSize: 18,
  },
  centeredContent: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  contentText: {
    color: 'black',
    fontSize: 18,
    textAlign: 'center',
  },
});
