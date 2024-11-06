// src/pages/Home.tsx

import React from 'react';
import { View, Text, StyleSheet, Image } from 'react-native';
import { useUser } from '../components/userContext';
import BitcoinChart from '../components/BitcoinChart';

const Home: React.FC = () => {
  const { username } = useUser();

  return (
    <View style={styles.container}>
      {/* Left Side - Text Content */}
      <View style={styles.textContainer}>
        <Text style={styles.title}>
          Welcome To TradeAgently {username}
        </Text>
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

      {/* Right Side ie. All the charts etc etc */}
      <View style={styles.rightContainer}>
        <BitcoinChart /> 
      </View>
    </View>
  );
};

export default Home;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'row', // Arrange horizontally
    justifyContent: 'space-between',
    alignItems: 'flex-start', // Align at the top
    backgroundColor: '#f0f0f0',
    padding: 20,
  },
  textContainer: {
    flex: 1, // Occupy half the width
    paddingRight: 20,
    justifyContent: 'flex-start',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
    textAlign: 'left',
  },
  description: {
    fontSize: 18,
    color: '#333',
    marginBottom: 15,
    lineHeight: 22,
    textAlign: 'left',
  },
  rightContainer: {
    flex: 1, // Occupy half the width
    alignItems: 'center',
    justifyContent: 'flex-start',
  },
  image: {
    width: '100%',
    height: 200,
    borderRadius: 10,
    marginBottom: 20,
    resizeMode: 'cover',
  },
});
