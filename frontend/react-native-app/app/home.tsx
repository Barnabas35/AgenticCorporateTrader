import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { useUser } from '../components/userContext'; 

const Home: React.FC = () => {
  const userName  = useUser();
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Welcome to the Investment Fund App {userName.username}</Text>
    </View>
  );
};

export default Home;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f0f0f0',
    width: '100%',
  },
  title: {
    fontSize: 24,
  },
});
