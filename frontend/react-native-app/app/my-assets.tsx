import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  FlatList,
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
  const [market, setMarket] = useState<'stocks' | 'crypto'>('stocks');
  const [sellModalVisible, setSellModalVisible] = useState(false);
  const [sellQuantity, setSellQuantity] = useState<string>('');
  const [selectedTicker, setSelectedTicker] = useState<string>('');
  const [reportModalVisible, setReportModalVisible] = useState(false);
  const [selectedReport, setSelectedReport] = useState<{
    ticker: string;
    profit: number | null;
    total_usd_invested: number | null;
  } | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [balance, setBalance] = useState<number | null>(null);
  const [buyTicker, setBuyTicker] = useState<string>('');
  const [buyUsdQuantity, setBuyUsdQuantity] = useState<string>('');

  const openSellModal = (ticker: string) => {
    setSelectedTicker(ticker);
    setSellModalVisible(true);
  };

  const closeSellModal = () => {
    setSellModalVisible(false);
    setSellQuantity('');
    setSelectedTicker('');
  };

  const fetchClientId = async () => {
    try {
      const response = await fetch('https://tradeagently.dev/get-client-list', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ session_token: sessionToken }),
      });

      const data = await response.json();

      if (data.status === 'Success' && data.clients?.length > 0) {
        setClientId(data.clients[0].client_id);
      } else {
        setError('Failed to fetch client list.');
      }
    } catch (err) {
      console.error('Error fetching client list:', err);
      setError('An error occurred while fetching the client list.');
    }
  };

  const fetchUserAssets = async () => {
    if (!clientId) return;

    try {
      const response = await fetch('https://tradeagently.dev/get-user-assets', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
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
            return { ticker, quantity, report: null };
          })
        );

        const filteredAssets = assetDetails.filter(
          (asset) => asset.quantity !== null && asset.quantity > 0
        );

        setAssets(filteredAssets);
      } else {
        setError('Failed to fetch assets.');
      }
    } catch (err) {
      console.error('Error fetching assets:', err);
      setError('An error occurred while fetching assets.');
    }
  };

  const fetchBalance = async () => {
    try {
      const response = await fetch('https://tradeagently.dev/get-balance', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
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

  const fetchAssetQuantity = async (ticker: string): Promise<number | null> => {
    try {
      const response = await fetch('https://tradeagently.dev/get-asset', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session_token: sessionToken,
          client_id: clientId,
          market,
          ticker,
        }),
      });

      const data = await response.json();
      return data.status === 'Success' ? data.total_asset_quantity : null;
    } catch (err) {
      console.error(`Error fetching asset quantity for ${ticker}:`, err);
      return null;
    }
  };

  const fetchAssetReport = async (ticker: string) => {
    try {
      console.log(sessionToken)
      console.log(clientId)
      console.log(market)
      console.log(ticker)
      const response = await fetch('https://tradeagently.dev/get-asset-report', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session_token: sessionToken,
          market: market,
          client_id: clientId,
          ticker: ticker,
        }),
      });

      const data = await response.json();
      console.log(data)

      if (data.status === 'Success') {
        setSelectedReport({
          ticker,
          profit: data.profit,
          total_usd_invested: data.total_usd_invested,
        });
        setReportModalVisible(true);
      } else {
        Alert.alert('Error', `Failed to fetch report for ${ticker}.`);
      }
    } catch (err) {
      console.error(`Error fetching report for ${ticker}:`, err);
      Alert.alert('Error', `An error occurred while fetching the report for ${ticker}.`);
    }
  };

  const handleSellAsset = async () => {
    if (!sellQuantity || isNaN(parseFloat(sellQuantity))) {
      Alert.alert('Error', 'Please enter a valid quantity.');
      return;
    }

    try {
      const response = await fetch('https://tradeagently.dev/sell-asset', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
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
        fetchUserAssets();
        closeSellModal();
      } else {
        Alert.alert('Error', 'Failed to sell asset.');
      }
    } catch (err) {
      console.error('Error selling asset:', err);
      Alert.alert('Error', 'An error occurred while selling the asset.');
    }
  };

  const handleBuyAsset = async () => {
    if (!buyTicker || !buyUsdQuantity || isNaN(parseFloat(buyUsdQuantity))) {
      Alert.alert('Error', 'Please enter a valid ticker and USD quantity.');
      return;
    }

    try {
      const response = await fetch('https://tradeagently.dev/purchase-asset', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session_token: sessionToken,
          client_id: clientId,
          market,
          ticker: buyTicker,
          usd_quantity: parseFloat(buyUsdQuantity),
        }),
      });

      const data = await response.json();

      if (data.status === 'Success') {
        Alert.alert('Success', `${buyUsdQuantity} USD of ${buyTicker} purchased successfully.`);
        fetchUserAssets();
        setBuyTicker('');
        setBuyUsdQuantity('');
      } else {
        Alert.alert('Error', 'Failed to purchase asset.');
      }
    } catch (err) {
      console.error('Error purchasing asset:', err);
      Alert.alert('Error', 'An error occurred while purchasing the asset.');
    }
  };

  useEffect(() => {
    if (sessionToken) {
      fetchClientId();
      fetchBalance();
    }
  }, [sessionToken]);

  useEffect(() => {
    if (clientId) {
      fetchUserAssets();
    }
  }, [clientId, market]);

  return (
    <View style={styles.container}>
      <View style={styles.balanceContainer}>
        <Text style={styles.balanceText}>Balance: ${balance?.toFixed(2) ?? 'N/A'}</Text>
      </View>

      <Text style={styles.title}>My Assets</Text>

      {error && <Text style={styles.errorText}>{error}</Text>}

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

      <View style={styles.buyContainer}>
        <TextInput
          style={[styles.buyInput, { width: '25%' }]}
          placeholder="Ticker Symbol"
          value={buyTicker}
          onChangeText={setBuyTicker}
        />
        <TextInput
          style={[styles.buyInput, { width: '25%' }]}
          placeholder="USD Amount"
          keyboardType="numeric"
          value={buyUsdQuantity}
          onChangeText={setBuyUsdQuantity}
        />
        <TouchableOpacity style={styles.buyButton} onPress={handleBuyAsset}>
          <Text style={styles.buyButtonText}>Buy</Text>
        </TouchableOpacity>
      </View>

      <FlatList
        data={assets}
        keyExtractor={(item, index) => `${item.ticker}-${index}`}
        renderItem={({ item }) => (
          <View style={styles.assetItem}>
            <View>
              <Text style={styles.assetText}>
                {item.ticker} - Amount: {item.quantity?.toFixed(5) ?? 'N/A'}
              </Text>
            </View>
            <View style={styles.actionButtons}>
              <TouchableOpacity
                style={styles.sellButton}
                onPress={() => openSellModal(item.ticker)}
              >
                <Text style={styles.sellButtonText}>Sell</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={styles.reportButton}
                onPress={() => fetchAssetReport(item.ticker)}
              >
                <Text style={styles.reportButtonText}>View Report</Text>
              </TouchableOpacity>
            </View>
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

      {/* Report Modal */}
      <Modal
        visible={reportModalVisible}
        animationType="slide"
        transparent={true}
        onRequestClose={() => setReportModalVisible(false)}
      >
        <View style={styles.modalBackground}>
          <View style={styles.modalContainer}>
            <Text style={styles.modalTitle}>Asset Report</Text>
            {selectedReport && (
              <View>
                <Text style={styles.modalText}>Ticker: {selectedReport.ticker}</Text>
                <Text style={styles.modalText}>
                  Profit: ${selectedReport.profit?.toFixed(2) ?? 'N/A'}
                </Text>
                <Text style={styles.modalText}>
                  Total Invested: ${selectedReport.total_usd_invested?.toFixed(2) ?? 'N/A'}
                </Text>
              </View>
            )}
            <TouchableOpacity
              style={styles.modalCloseButton}
              onPress={() => setReportModalVisible(false)}
            >
              <Text style={styles.modalCloseButtonText}>Close</Text>
            </TouchableOpacity>
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
  buyContainer: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
    alignItems: 'center',
    marginBottom: 20,
  },
  buyInput: {
    borderColor: '#ccc',
    borderWidth: 1,
    borderRadius: 5,
    paddingHorizontal: 10,
    height: 40,
    marginRight: 10,
  },
  buyButton: {
    backgroundColor: '#28a745',
    padding: 10,
    borderRadius: 5,
    alignItems: 'center',
  },
  buyButtonText: {
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
  actionButtons: {
    flexDirection: 'row',
    gap: 10,
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
  reportButton: {
    backgroundColor: '#007bff',
    paddingVertical: 5,
    paddingHorizontal: 10,
    borderRadius: 5,
  },
  reportButtonText: {
    color: '#fff',
    fontWeight: 'bold',
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
  modalCloseButton: {
    backgroundColor: '#007bff',
    padding: 10,
    borderRadius: 5,
    marginTop: 10,
  },
  modalCloseButtonText: {
    color: 'white',
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
});

export default MyAssets;
