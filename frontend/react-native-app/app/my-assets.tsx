import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  FlatList,
  ActivityIndicator,
  StyleSheet,
  TouchableOpacity,
  TextInput,
  Alert,
  Modal,
} from 'react-native';
import { useSessionToken } from '../components/userContext';

interface Asset {
  ticker: string;
  quantity: number | null;
  report: { profit: number | null; total_usd_invested: number | null } | null;
}

const MyAssets: React.FC = () => {
  const [sessionToken] = useSessionToken();
  const [clientId, setClientId] = useState<string | null>(null);
  const [assets, setAssets] = useState<Asset[]>([]);
  const [market, setMarket] = useState<'stocks' | 'crypto'>('stocks'); // Default to "stocks"
  const [ticker, setTicker] = useState<string>(''); // Ticker symbol for purchase
  const [usdQuantity, setUsdQuantity] = useState<string>(''); // Amount in USD for purchase
  const [sellModalVisible, setSellModalVisible] = useState(false);
  const [sellQuantity, setSellQuantity] = useState<string>('');
  const [selectedTicker, setSelectedTicker] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [balance, setBalance] = useState<number | null>(null);

  // Fetch client list to get the client_id
  const fetchClientId = async () => {
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

      if (data.status === 'Success' && data.clients?.length > 0) {
        setClientId(data.clients[0].client_id); // Use the first client ID
      } else {
        setError('Failed to fetch client list.');
      }
    } catch (err) {
      console.error('Error fetching client list:', err);
      setError('An error occurred while fetching the client list.');
    } finally {
      setLoading(false);
    }
  };

  // Fetch user assets for the client
  const fetchUserAssets = async () => {
    if (!clientId) return;

    setLoading(true);
    setError(null);

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
        const assetDetails = await Promise.all(
          data.ticker_symbols.map(async (ticker: string) => {
            const quantity = await fetchAssetQuantity(ticker);
            const report = await fetchAssetReport(ticker);
            return { ticker, quantity, report };
          })
        );
        setAssets(assetDetails);
      } else {
        setError('Failed to fetch assets.');
      }
    } catch (err) {
      console.error('Error fetching assets:', err);
      setError('An error occurred while fetching assets.');
    } finally {
      setLoading(false);
    }
  };

  // Fetch balance
  const fetchBalance = async () => {
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
        setError('Failed to fetch balance.');
      }
    } catch (err) {
      console.error('Error fetching balance:', err);
      setError('An error occurred while fetching balance.');
    }
  };
  // Fetch individual asset quantity
  const fetchAssetQuantity = async (ticker: string): Promise<number | null> => {
    try {
      const response = await fetch('https://tradeagently.dev/get-asset', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          session_token: sessionToken,
          client_id: clientId,
          market,
          ticker,
        }),
      });

      const data = await response.json();

      if (data.status === 'Success') {
        return data.total_asset_quantity;
      } else {
        console.error(`Failed to fetch asset quantity for ${ticker}`);
        return null;
      }
    } catch (err) {
      console.error(`Error fetching asset quantity for ${ticker}:`, err);
      return null;
    }
  };

  // Fetch asset report
  const fetchAssetReport = async (ticker: string) => {
    try {
      const response = await fetch('https://tradeagently.dev/get-asset-report', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          session_token: sessionToken,
          client_id: clientId,
          market,
          ticker_symbol: ticker,
        }),
      });

      const data = await response.json();

      if (data.status === 'Success') {
        return { profit: data.profit, total_usd_invested: data.total_usd_invested };
      } else {
        console.error(`Failed to fetch asset report for ${ticker}`);
        return { profit: null, total_usd_invested: null };
      }
    } catch (err) {
      console.error(`Error fetching asset report for ${ticker}:`, err);
      return { profit: null, total_usd_invested: null };
    }
  };

  // Open sell modal
  const openSellModal = (ticker: string) => {
    setSelectedTicker(ticker);
    setSellModalVisible(true);
  };

  // Close sell modal
  const closeSellModal = () => {
    setSellModalVisible(false);
    setSellQuantity('');
    setSelectedTicker('');
  };

  // Handle asset selling
  const handleSellAsset = async () => {
    if (!sellQuantity || isNaN(parseFloat(sellQuantity))) {
      Alert.alert('Error', 'Please enter a valid quantity.');
      return;
    }

    try {
      const response = await fetch('https://tradeagently.dev/sell-asset', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          session_token: sessionToken,
          client_id: clientId,
          market,
          ticker: selectedTicker,
          asset_quantity: parseFloat(sellQuantity),
        }),
      });

      const data = await response.json();

      if (data.status === 'Success') {
        Alert.alert('Success', `${sellQuantity} of ${selectedTicker} sold successfully.`);
        fetchUserAssets(); // Refresh the assets
        closeSellModal();
      } else {
        console.error('Failed to sell asset.', data);
        Alert.alert('Error', 'Failed to sell asset.');
      }
    } catch (err) {
      console.error('Error selling asset:', err);
      Alert.alert('Error', 'An error occurred while selling the asset.');
    }
  };
  useEffect(() => {
    if (sessionToken) {
      fetchClientId();
      fetchBalance();
    }
  }, [sessionToken]);

  // Fetch user assets when clientId changes or market changes
  useEffect(() => {
    if (clientId) {
      fetchUserAssets();
    }
  }, [clientId, market]);
  return (
    <View style={styles.container}>
      {/* Balance Display */}
      <View style={styles.balanceContainer}>
        <Text style={styles.balanceText}>Balance: ${balance?.toFixed(2) ?? 'N/A'}</Text>
      </View>

      <Text style={styles.title}>My Assets</Text>

      {error && <Text style={styles.errorText}>{error}</Text>}

      {/* Market Switch */}
      <View style={styles.marketSwitchContainer}>
        <TouchableOpacity
          style={[styles.marketButton, market === 'stocks' && styles.activeMarketButton]}
          onPress={() => setMarket('stocks')}
        >
          <Text style={styles.marketButtonText}>Stocks</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.marketButton, market === 'crypto' && styles.activeMarketButton]}
          onPress={() => setMarket('crypto')}
        >
          <Text style={styles.marketButtonText}>Crypto</Text>
        </TouchableOpacity>
      </View>

      {/* Assets List */}
      <FlatList
        data={assets}
        keyExtractor={(item, index) => `${item.ticker}-${index}`}
        renderItem={({ item }) => (
          <View style={styles.assetItem}>
            <View>
              <Text style={styles.assetText}>
                {item.ticker} - Amount: {item.quantity !== null ? item.quantity.toFixed(5) : 'N/A'}
              </Text>
              {item.report && (
                <Text style={styles.reportText}>
                  Profit: ${item.report.profit?.toFixed(2) || 'N/A'} | Total Invested: $
                  {item.report.total_usd_invested?.toFixed(2) || 'N/A'}
                </Text>
              )}
            </View>
            <TouchableOpacity
              style={styles.sellButton}
              onPress={() => openSellModal(item.ticker)}
            >
              <Text style={styles.sellButtonText}>Sell</Text>
            </TouchableOpacity>
          </View>
        )}
        ListEmptyComponent={<Text style={styles.emptyText}>No assets found.</Text>}
      />

      {/* Sell Modal */}
      <Modal
        visible={sellModalVisible}
        animationType="slide"
        transparent={true}
        onRequestClose={closeSellModal}
      >
        <View style={styles.modalBackground}>
          <View style={styles.modalContainer}>
            <Text style={styles.modalTitle}>Sell Asset</Text>
            <Text style={styles.modalText}>Enter the quantity of {selectedTicker} to sell:</Text>
            <TextInput
              style={styles.modalInput}
              placeholder="Quantity"
              keyboardType="numeric"
              value={sellQuantity}
              onChangeText={setSellQuantity}
            />
            <View style={styles.modalButtonContainer}>
              <TouchableOpacity style={styles.modalButton} onPress={handleSellAsset}>
                <Text style={styles.modalButtonText}>Sell</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.modalButton, styles.modalCancelButton]}
                onPress={closeSellModal}
              >
                <Text style={styles.modalButtonText}>Cancel</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#f5f5f5',
    width: '100%',
  },
  balanceContainer: {
    padding: 15,
    backgroundColor: '#007bff',
    borderRadius: 10,
    marginBottom: 20,
    alignSelf: 'flex-start',
  },
  balanceText: {
    color: 'white',
    fontWeight: 'bold',
    fontSize: 16,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
    textAlign: 'center',
    color: '#333',
  },
  marketSwitchContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    marginBottom: 20,
  },
  marketButton: {
    padding: 10,
    borderRadius: 5,
    backgroundColor: '#ddd',
    marginHorizontal: 5,
    width: 120,
    alignItems: 'center',
  },
  activeMarketButton: {
    backgroundColor: '#007bff',
  },
  marketButtonText: {
    color: '#fff',
    fontWeight: 'bold',
  },
  assetItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 15,
    backgroundColor: '#fff',
    borderRadius: 5,
    marginBottom: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  assetText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
  },
  reportText: {
    fontSize: 14,
    color: '#666',
    marginTop: 5,
  },
  sellButton: {
    backgroundColor: '#FF5555',
    paddingVertical: 5,
    paddingHorizontal: 10,
    borderRadius: 5,
  },
  sellButtonText: {
    color: '#fff',
    fontWeight: 'bold',
  },
  emptyText: {
    textAlign: 'center',
    color: '#aaa',
    marginTop: 20,
  },
  errorText: {
    color: 'red',
    marginBottom: 10,
    textAlign: 'center',
  },
  modalBackground: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
  },
  modalContainer: {
    backgroundColor: 'white',
    padding: 20,
    borderRadius: 10,
    width: '80%',
    alignItems: 'center',
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 10,
  },
  modalText: {
    fontSize: 16,
    marginBottom: 10,
  },
  modalInput: {
    height: 40,
    borderColor: '#ccc',
    borderWidth: 1,
    borderRadius: 5,
    paddingHorizontal: 10,
    width: '100%',
    marginBottom: 10,
  },
  modalButtonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    width: '100%',
  },
  modalButton: {
    backgroundColor: '#007bff',
    padding: 10,
    borderRadius: 5,
    width: '40%',
    alignItems: 'center',
  },
  modalCancelButton: {
    backgroundColor: '#ccc',
  },
  modalButtonText: {
    color: 'white',
    fontWeight: 'bold',
  },
});

export default MyAssets;
