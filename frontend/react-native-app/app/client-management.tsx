import React, { useEffect, useState } from 'react';
import { View, Text, Button, TextInput, FlatList, TouchableOpacity, Alert, StyleSheet } from 'react-native';
import { useSessionToken } from '../components/userContext';

const ClientManagement: React.FC = () => {
  const [sessionToken] = useSessionToken(); // Get session token from user context
  const [clients, setClients] = useState<any[]>([]);
  const [newClientName, setNewClientName] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Fetch client list from the API
  const fetchClientList = async () => {
    setLoading(true);
    setError(null);

    console.log("Session Token for fetchClientList:", sessionToken); // Troubleshooting: Print session token

    try {
      const response = await fetch('https://tradeagently.dev/get-client-list', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ session_token: sessionToken }),
      });

      const data = await response.json();
      console.log("Response from fetchClientList:", data); // Troubleshooting: Print API response

      if (data.status === 'Success') {
        setClients(data.clients);
      } else {
        setError('Failed to fetch clients.');
      }
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('An unexpected error occurred.');
      }
    } finally {
      setLoading(false);
    }
  };

  // Add a new client
  const addClient = async () => {
    if (!newClientName.trim()) {
      Alert.alert('Error', 'Client name cannot be empty.');
      return;
    }

    console.log("Session Token for addClient:", sessionToken); // Troubleshooting: Print session token
    console.log("Client Name being added:", newClientName); // Troubleshooting: Print client name

    try {
      const response = await fetch('https://tradeagently.dev/add-client', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          session_token: sessionToken,
          client_name: newClientName,
        }),
      });

      const data = await response.json();
      console.log("Response from addClient:", data); // Troubleshooting: Print API response

      if (data.status === 'Success') {
        setClients((prevClients) => [...prevClients, { client_name: newClientName, id: Date.now().toString() }]);
        setNewClientName('');
      } else {
        Alert.alert('Error', 'Failed to add client.');
      }
    } catch (err) {
      Alert.alert('Error', 'An error occurred while adding the client.');
    }
  };

  // Remove a client
  const removeClient = async (clientName: string) => {
    console.log("Session Token for removeClient:", sessionToken); // Troubleshooting: Print session token
    console.log("Client Name being removed:", clientName); // Troubleshooting: Print client name

    try {
      const response = await fetch('https://tradeagently.dev/remove-client', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          session_token: sessionToken,
          client_name: clientName,
        }),
      });

      const data = await response.json();
      console.log("Response from removeClient:", data); // Troubleshooting: Print API response

      if (data.status === 'Success') {
        setClients((prevClients) => prevClients.filter((client) => client.client_name !== clientName));
      } else {
        Alert.alert('Error', 'Failed to remove client.');
      }
    } catch (err) {
      Alert.alert('Error', 'An error occurred while removing the client.');
    }
  };

  // Fetch clients when component mounts
  useEffect(() => {
    if (sessionToken) {
      fetchClientList();
    } else {
      setError('Session token is missing.');
    }
  }, [sessionToken]);

  return (
    <View style={styles.pageContainer}>
      <View style={styles.container}>
        <Text style={styles.title}>Client Management</Text>

        {/* Add Client Section */}
        <View style={styles.addClientContainer}>
          <TextInput
            style={styles.input}
            placeholder="Enter new client name"
            value={newClientName}
            onChangeText={setNewClientName}
          />
          <Button title="Add Client" onPress={addClient} color="green" />
        </View>

        {/* Client List */}
        {loading ? (
          <Text>Loading...</Text>
        ) : error ? (
          <Text>Error: {error}</Text>
        ) : (
          <FlatList
            data={clients}
            keyExtractor={(item) => item.id} // Use client ID as unique key
            renderItem={({ item }) => (
              <View style={styles.clientItem}>
                <Text>{item.client_name}</Text>
                <TouchableOpacity onPress={() => removeClient(item.client_name)}>
                  <Text style={styles.removeButton}>❌</Text>
                </TouchableOpacity>
              </View>
            )}
          />
        )}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  pageContainer: {
    flex: 1,
    alignItems: 'center',
    backgroundColor: '#f0f0f0',
    padding: 20,
  },
  container: {
    width: '120%',
    backgroundColor: '#fff',
    padding: 20,
    borderRadius: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 5,
    elevation: 5,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
    textAlign: 'center',
  },
  addClientContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
  },
  input: {
    flex: 1,
    height: 40,
    borderColor: '#ccc',
    borderWidth: 1,
    borderRadius: 8,
    paddingHorizontal: 10,
    marginRight: 10,
  },
  clientItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 10,
    borderBottomColor: '#ccc',
    borderBottomWidth: 1,
  },
  removeButton: {
    color: 'red',
    fontSize: 18,
  },
});

export default ClientManagement;
