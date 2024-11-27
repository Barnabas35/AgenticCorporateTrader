import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  Button,
  TextInput,
  FlatList,
  TouchableOpacity,
  Alert,
  StyleSheet,
} from 'react-native';
import { useSessionToken } from '../components/userContext';
import { useNavigate } from 'react-router-dom';

const ClientManagement: React.FC = () => {
  const [sessionToken] = useSessionToken(); // Get session token from user context
  const [clients, setClients] = useState<any[]>([]);
  const [selectedClientId, setSelectedClientId] = useState<string | null>(null);
  const [assets, setAssets] = useState<string[]>([]);
  const [purchaseAmount, setPurchaseAmount] = useState<string>(''); // Amount for purchase
  const [market, setMarket] = useState<string>('stocks'); // Default market
  const [ticker, setTicker] = useState<string>(''); // Asset ticker
  const [newClientName, setNewClientName] = useState<string>(''); // New client name
  const [activeTab, setActiveTab] = useState<string>('Manage Assets'); // Active tab
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  if (!sessionToken) {
    // If not logged in, redirect to login
    navigate('/login');
    return null;
  }

  // Fetch client list from the API
  const fetchClientList = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetch('https://tradeagently.dev/get-client-list', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ session_token: sessionToken }),
      });

      const data = await response.json();
      if (data.status === 'Success') {
        setClients(data.clients);
      } else {
        setError('Failed to fetch clients.');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unexpected error occurred.');
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
      if (data.status === 'Success') {
        setClients((prevClients) => [...prevClients, { client_name: newClientName, client_id: Date.now().toString() }]);
        setNewClientName('');
        Alert.alert('Success', 'Client added successfully!');
      } else {
        Alert.alert('Error', 'Failed to add client.');
      }
    } catch (err) {
      Alert.alert('Error', 'An error occurred while adding the client.');
    }
  };

  const removeClient = async (clientName: string) => {
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
      if (data.status === 'Success') {
        setClients((prevClients) => prevClients.filter((client) => client.client_name !== clientName));
        Alert.alert('Success', 'Client removed successfully!');
        if (selectedClientId === clientName) setSelectedClientId(null);
      } else {
        Alert.alert('Error', 'Failed to remove client.');
      }
    } catch (err) {
      Alert.alert('Error', 'An error occurred while removing the client.');
    }
  };
  

  // Fetch user assets for a client
  const fetchUserAssets = async (clientId: string) => {
    try {
      const response = await fetch('https://tradeagently.dev/get-user-assets', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          session_token: sessionToken,
          client_id: clientId,
          market,
        }),
      });

      const data = await response.json();
      if (data.status === 'Success') {
        setAssets(data.ticker_symbols);
        Alert.alert('Success', `Assets fetched for client ${clientId}.`);
      } else {
        Alert.alert('Error', 'Failed to fetch user assets.');
      }
    } catch (err) {
      Alert.alert('Error', 'An error occurred while fetching user assets.');
    }
  };

  // Purchase asset for a client
  const purchaseAsset = async () => {
    if (!selectedClientId || !ticker || !purchaseAmount) {
      Alert.alert('Error', 'Please select a client, enter a valid ticker, and amount.');
      return;
    }

    try {
      const response = await fetch('https://tradeagently.dev/purchase-asset', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          session_token: sessionToken,
          usd_quantity: parseFloat(purchaseAmount),
          market,
          ticker,
          client_id: selectedClientId,
        }),
      });

      const data = await response.json();
      if (data.status === 'Success') {
        Alert.alert('Success', `Purchased ${ticker} for $${purchaseAmount}.`);
        setPurchaseAmount('');
        setTicker('');
      } else {
        Alert.alert('Error', 'Failed to purchase asset.');
      }
    } catch (err) {
      Alert.alert('Error', 'An error occurred while purchasing the asset.');
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

        {/* Tabs */}
        <View style={styles.tabsContainer}>
          <TouchableOpacity
            style={[styles.tab, activeTab === 'Manage Assets' && styles.activeTab]}
            onPress={() => setActiveTab('Manage Assets')}
          >
            <Text style={styles.tabText}>Manage Assets</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.tab, activeTab === 'Add Clients' && styles.activeTab]}
            onPress={() => setActiveTab('Add Clients')}
          >
            <Text style={styles.tabText}>Add Clients</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.tab, activeTab === 'Delete Clients' && styles.activeTab]}
            onPress={() => setActiveTab('Delete Clients')}
          >
            <Text style={styles.tabText}>Delete Clients</Text>
          </TouchableOpacity>
        </View>

        {/* Tab Content */}
        {activeTab === 'Manage Assets' && (
          <>
            {/* Client List */}
            {loading ? (
              <Text>Loading...</Text>
            ) : error ? (
              <Text>Error: {error}</Text>
            ) : (
              <FlatList
                data={clients}
                keyExtractor={(item) => item.client_id}
                renderItem={({ item }) => (
                  <TouchableOpacity
                    style={[
                      styles.clientItem,
                      selectedClientId === item.client_id && styles.selectedClientItem,
                    ]}
                    onPress={() => {
                      setSelectedClientId(item.client_id);
                      fetchUserAssets(item.client_id);
                    }}
                  >
                    <Text>{item.client_name}</Text>
                  </TouchableOpacity>
                )}
              />
            )}

            {/* Selected Client Actions */}
            {selectedClientId && (
              <View>
                <Text style={styles.sectionTitle}>Manage Assets for Client</Text>

                {/* Purchase Asset Section */}
                <View style={styles.assetManagementContainer}>
                  <TextInput
                    style={styles.input}
                    placeholder="Enter ticker (e.g., BTC)"
                    value={ticker}
                    onChangeText={setTicker}
                  />
                  <TextInput
                    style={styles.input}
                    placeholder="Amount in USD"
                    keyboardType="numeric"
                    value={purchaseAmount}
                    onChangeText={setPurchaseAmount}
                  />
                  <Button title="Purchase Asset" onPress={purchaseAsset} color="green" />
                </View>

                {/* User Assets */}
                <Text style={styles.sectionTitle}>Assets</Text>
                <FlatList
                  data={assets}
                  keyExtractor={(item, index) => `${item}-${index}`}
                  renderItem={({ item }) => <Text style={styles.assetItem}>{item}</Text>}
                />
              </View>
            )}
          </>
        )}

        {activeTab === 'Add Clients' && (
          <View style={styles.addClientContainer}>
            <TextInput
              style={styles.input}
              placeholder="Enter new client name"
              value={newClientName}
              onChangeText={setNewClientName}
            />
            <Button title="Add Client" onPress={addClient} color="green" />
          </View>
        )}

        {activeTab === 'Delete Clients' && (
          <FlatList
            data={clients}
            keyExtractor={(item) => item.client_id}
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
    width: '120%',
  },
  container: {
    width: '50%',
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
  tabsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 20,
  },
  tab: {
    flex: 1,
    paddingVertical: 10,
    borderBottomWidth: 2,
    borderBottomColor: '#ccc',
    alignItems: 'center',
  },
  activeTab: {
    borderBottomColor: '#007bff',
  },
  tabText: {
    fontSize: 16,
    color: '#333',
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    marginVertical: 10,
  },
  input: {
    height: 40,
    borderColor: '#ccc',
    borderWidth: 1,
    borderRadius: 8,
    paddingHorizontal: 10,
    marginBottom: 10,
    width: '100%',
  },
  clientItem: {
    paddingVertical: 10,
    paddingHorizontal: 10,
    borderBottomColor: '#ccc',
    borderBottomWidth: 1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  selectedClientItem: {
    backgroundColor: '#e0ffe0',
  },
  addClientContainer: {
    marginTop: 20,
  },
  assetManagementContainer: {
    marginVertical: 20,
  },
  assetItem: {
    paddingVertical: 5,
    fontSize: 16,
  },
  removeButton: {
    color: 'red',
    fontSize: 18,
  },
});

export default ClientManagement;
