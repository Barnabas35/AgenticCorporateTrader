import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { useUser } from '../components/userContext';

const Home: React.FC = () => {
  const userName = useUser();
  
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Welcome to the Investment Fund App, {userName.username}</Text>
      <Text style={styles.description}>
        This platform is designed to help users efficiently manage and track their investments. 
        Whether you're new to investing or an experienced fund manager, the app provides a comprehensive 
        suite of tools to assist you in making informed financial decisions.
      </Text>
      <Text style={styles.description}>
        Key features include real-time tracking of investment performance, detailed analysis of 
        fund allocations, and a user-friendly interface that simplifies managing portfolios. 
        With a focus on data-driven insights, our platform aims to empower users to achieve 
        their financial goals with ease.
      </Text>
      <Text style={styles.description}>
        To get started, log in or register, and begin tracking your investments today!
      </Text>
    </View>
  );
};

export default Home;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'flex-start',
    alignItems: 'center',
    backgroundColor: '#f0f0f0',
    width: '100%',
    paddingTop: 50,
    paddingHorizontal: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
  },
  description: {
    fontSize: 18,
    color: '#333',
    textAlign: 'center',
    marginBottom: 15,
    lineHeight: 22,
  },
});
