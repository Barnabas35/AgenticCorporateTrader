import React from 'react';
import { View, Text, StyleSheet, ScrollView } from 'react-native';

const About: React.FC = () => {
  return (
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.box}>
        <Text style={styles.title}>About Trade Agently</Text>
      </View>

      <View style={styles.box}>
        <Text style={styles.sectionTitle}>Who Are We?</Text>
        <Text style={styles.paragraph}>
          Welcome to Trade Agently! We're a group of enthusiastic developers and business minds who came together to build an app that makes trading and managing your investments easier and more efficient. Our team is composed of students from diverse disciplines, each bringing their unique expertise to the table.
        </Text>
      </View>

      <View style={styles.box}>
        <Text style={styles.sectionTitle}>Our Mission</Text>
        <Text style={styles.paragraph}>
          Our mission is to empower users to make informed decisions in the world of trading. Whether you're keeping an eye on stocks, cryptocurrencies, or managing client portfolios, our app aims to simplify these complex processes through an intuitive and user-friendly experience.
        </Text>
      </View>

      <View style={styles.box}>
        <Text style={styles.sectionTitle}>A Collaborative Effort</Text>
        <Text style={styles.paragraph}>
          This app was developed as part of a group project for our university coursework. Each team member played a critical role—from front-end development to back-end integration, and everything in between. This collaboration has allowed us to create a well-rounded product that reflects our combined strengths.
        </Text>
        <Text style={styles.paragraph}>Here are some fun facts about our group:</Text>
        <Text style={styles.listItem}>• Our team consists of 4 members with backgrounds in business and computer science.</Text>
        <Text style={styles.listItem}>• We spent over 100 hours designing, coding, and refining this website, backend, AI Agents and the mobile app.</Text>
        <Text style={styles.listItem}>• The project has helped us learn a lot about teamwork, version control, and the challenges of building a full-stack application.</Text>
      </View>

      <View style={styles.box}>
        <Text style={styles.sectionTitle}>Contact Us</Text>
        <Text style={styles.paragraph}>
          We'd love to hear from you! If you have questions, suggestions, or just want to say hello, feel free to reach out to us at:
        </Text>
        <Text style={styles.email}>contact@tradeagently.com</Text>
      </View>

      <View style={styles.box}>
        <Text style={styles.sectionTitle}>Thank You!</Text>
        <Text style={styles.paragraph}>
          We want to thank our mentors, classmates, and friends who supported us throughout this journey. We hope Trade Agently helps you achieve your trading goals. Happy trading!
        </Text>
      </View>
    </ScrollView>
  );
};

export default About;

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    padding: 20,
    backgroundColor: '#f0f0f0',
    alignItems: 'center',
  },
  box: {
    backgroundColor: '#ffffff',
    borderRadius: 10,
    padding: 20,
    marginBottom: 20,
    width: '90%',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 5, // For Android shadow
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    marginBottom: 10,
    color: '#2e86de', // Blue color for the title
    textAlign: 'center',
  },
  sectionTitle: {
    fontSize: 22,
    fontWeight: 'bold',
    marginBottom: 10,
    color: '#333333',
    textAlign: 'center',
  },
  paragraph: {
    fontSize: 16,
    textAlign: 'center',
    marginBottom: 10,
    color: '#555555',
    lineHeight: 24,
  },
  listItem: {
    fontSize: 16,
    textAlign: 'left',
    color: '#444444',
    marginVertical: 5,
    width: '100%',
  },
  email: {
    fontSize: 16,
    color: '#e74c3c', // Red color to make the email stand out
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 20,
  },
});
