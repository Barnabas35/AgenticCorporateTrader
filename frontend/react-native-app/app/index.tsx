import React from 'react';
import { View, Text, StyleSheet, Image } from 'react-native';
import { useUser } from '../components/userContext';
import BitcoinChart from '../components/BitcoinChart';
import { FontAwesome5, MaterialIcons } from '@expo/vector-icons';

const Home: React.FC = () => {
  const { username } = useUser();

  return (
    <View style={styles.container}>
      {/* Left Side - Text Content */}
      <View style={styles.textContainer}>
        <View style={styles.headerContainer}>
          <Text style={styles.title}>
            Welcome to TradeAgently, {username}!
          </Text>
        </View>

        <Text style={styles.description}>
          This platform is designed to help users efficiently manage and track their investments.
          Whether you're new to investing or an experienced fund manager, the app provides a comprehensive
          suite of tools to assist you in making informed financial decisions.
        </Text>

        <View style={styles.featureContainer}>
          <MaterialIcons name="show-chart" size={28} color="#2e86de" />
          <Text style={styles.featureText}>
            Real-time tracking of investment performance to keep you ahead.
          </Text>
        </View>

        <View style={styles.featureContainer}>
          <FontAwesome5 name="chart-pie" size={28} color="#e74c3c" />
          <Text style={styles.featureText}>
            Detailed analysis of fund allocations for effective decision-making.
          </Text>
        </View>

        <View style={styles.featureContainer}>
          <MaterialIcons name="touch-app" size={28} color="#27ae60" />
          <Text style={styles.featureText}>
            User-friendly interface that simplifies managing portfolios.
          </Text>
        </View>

        <Text style={styles.description}>
          To get started, log in or register, and begin tracking your investments today!
        </Text>
      </View>

      {/* Right Side - Charts and More */}
      <View style={styles.rightContainer}>
        <BitcoinChart />
        <View style={styles.chartInfoContainer}>
          <Text style={styles.chartTitle}>Bitcoin Market Overview</Text>
          <Text style={styles.chartDescription}>
            Track the performance of Bitcoin in real time with our live charts. Stay informed on the latest trends and make smarter investment decisions.
          </Text>
        </View>
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
  headerContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 20,
  },
  headerImage: {
    width: 60,
    height: 60,
    borderRadius: 30,
    marginRight: 10,
    resizeMode: 'cover',
  },
  title: {
    fontSize: 26,
    fontWeight: 'bold',
    textAlign: 'left',
    color: '#2e86de',
  },
  description: {
    fontSize: 18,
    color: '#333',
    marginBottom: 15,
    lineHeight: 26,
    textAlign: 'left',
  },
  featureContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 15,
  },
  featureText: {
    fontSize: 18,
    color: '#333',
    marginLeft: 10,
    lineHeight: 24,
  },
  rightContainer: {
    flex: 1, // Occupy half the width
    alignItems: 'center',
    justifyContent: 'flex-start',
  },
  chartInfoContainer: {
    marginTop: 20,
    alignItems: 'center',
  },
  chartTitle: {
    fontSize: 22,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 10,
  },
  chartDescription: {
    fontSize: 16,
    color: '#555',
    textAlign: 'center',
    lineHeight: 22,
  },
});
