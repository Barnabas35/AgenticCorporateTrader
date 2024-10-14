import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

const contact: React.FC = () => {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Call me</Text>
    </View>
  );
};

export default contact;

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
