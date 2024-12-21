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

/**
 * CSV file link. We'll fetch from this URL, then create a Blob
 * and trigger a download on the user's browser.
 */
const FILE_URL = `https://storage.googleapis.com/agenticcorporatetrader.appspot.com/user_reports/hello.csv?Expires=1794811810&GoogleAccessId=firebase-adminsdk-hms3b%40agenticcorporatetrader.iam.gserviceaccount.com&Signature=GMNNOBlEiZeiHcxjXfNRlcMko2DPfF93sEahbBxrq1FozyX5Mbp2P94Hp%2B3oEGwXHGJwyfNGoD%2F16khRPuAtxOc%2BwkEcb8xz0CieyGC8uDzbuO1ATpo5K1CsXkcZA61UXwh%2FsSCIFPlK%2BVxdd8Iy8SV56Wltt9eCxKU835mvdSrv7i1WfIrflAMF5Ktu9mjx7jrUb6yL86Mr1fNJpV5Ff4hNSxcWfohdoXBCq97xEKTvH4LoI5mv%2F7rEyh0pUpYQUdcUUo%2FYy5jJOwC7xfLI4TRJhxX8OOsOXMFkjgGAzghSxI%2Fpfs%2BSMKiVIED2OupHNUPtfB65j1zM7EKdlNt%2BRg%3D%3D`;

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

  // For Buy Modal
  const [buyModalVisible, setBuyModalVisible] = useState(false);
  const [buyUsdQuantity, setBuyUsdQuantity] = useState<string>('');

  // Track selected asset quantity when selling
  const [selectedAssetQuantity, setSelectedAssetQuantity] = useState<number | null>(null);

  // Validation error state for sell modal
  const [sellModalError, setSellModalError] = useState<string>('');

  // ------------------------------------------------------------
  // 1) Download CSV on Web: fetch -> Blob -> hidden <a> -> click
  // ------------------------------------------------------------
  const handleDownloadAllAssetCsv = async () => {
    try {
      // 1) Fetch the file from the server as text or blob
      const response = await fetch(FILE_URL);
      if (!response.ok) {
        throw new Error('Failed to fetch CSV');
      }

      // Option A: Read as text, then convert to a Blob
      const csvText = await response.text();
      const blob = new Blob([csvText], { type: 'text/csv' });

      // Option B: Or read as blob directly:
      //   const blob = await response.blob();

      // 2) Create a local URL for that blob
      const url = URL.createObjectURL(blob);

      // 3) Create an <a> element programmatically
      const link = document.createElement('a');
      link.href = url;
      link.download = 'all_assets.csv'; // The filename for the saved file

      // 4) Append, click, remove
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);

      // 5) Revoke the blob URL (clean up)
      URL.revokeObjectURL(url);

    } catch (err) {
      console.error('Error downloading CSV:', err);
      Alert.alert('Error', 'An error occurred while downloading the CSV.');
    }
  };

  // The rest of your MyAssets logic, e.g.:
  const openSellModal = (ticker: string) => {
    setSelectedTicker(ticker);
    const asset = assets.find((a) => a.ticker === ticker);
    if (asset && asset.quantity !== null) {
      setSelectedAssetQuantity(asset.quantity);
    } else {
      setSelectedAssetQuantity(null);
    }
    setSellQuantity('');
    setSellModalError('');
    setSellModalVisible(true);
  };

  const closeSellModal = () => {
    setSellModalVisible(false);
    setSellQuantity('');
    setSelectedTicker('');
    setSelectedAssetQuantity(null);
    setSellModalError('');
  };

  const openBuyModal = (ticker: string) => {
    setSelectedTicker(ticker);
    setBuyModalVisible(true);
  };

  const closeBuyModal = () => {
    setBuyModalVisible(false);
    setBuyUsdQuantity('');
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
      const response = await fetch('https://tradeagently.dev/get-asset-report', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session_token: sessionToken,
          market,
          client_id: clientId,
          ticker: ticker,
        }),
      });
      const data = await response.json();

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
    const quantity = parseFloat(sellQuantity);

    if (!sellQuantity.trim()) {
      setSellModalError('Please input an amount.');
      return;
    }
    if (isNaN(quantity) || quantity <= 0) {
      setSellModalError('Please input a valid amount.');
      return;
    }
    if (selectedAssetQuantity !== null && quantity > selectedAssetQuantity) {
      setSellModalError(
        `You can only sell up to ${selectedAssetQuantity.toFixed(5)} units of ${selectedTicker}.`
      );
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
          asset_quantity: quantity,
        }),
      });

      const data = await response.json();
      if (data.status === 'Success') {
        Alert.alert('Success', `${quantity} of ${selectedTicker} sold successfully.`);
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
    if (!buyUsdQuantity || isNaN(parseFloat(buyUsdQuantity)) || parseFloat(buyUsdQuantity) <= 0) {
      Alert.alert('Error', 'Please enter a valid USD amount.');
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
          ticker: selectedTicker,
          usd_quantity: parseFloat(buyUsdQuantity),
        }),
      });

      const data = await response.json();
      if (data.status === 'Success') {
        Alert.alert('Success', `${buyUsdQuantity} USD of ${selectedTicker} purchased successfully.`);
        fetchUserAssets();
        closeBuyModal();
      } else {
        Alert.alert('Error', 'Failed to purchase asset.');
      }
    } catch (err) {
      console.error('Error purchasing asset:', err);
      Alert.alert('Error', 'An error occurred while purchasing the asset.');
    }
  };

  const handleSellAll = () => {
    if (selectedAssetQuantity !== null) {
      setSellQuantity(selectedAssetQuantity.toString());
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
      {/** CSV download button in the top-right */}
      <TouchableOpacity style={styles.downloadCsvButton} onPress={handleDownloadAllAssetCsv}>
        <Text style={styles.downloadCsvButtonText}>Download All Asset CSV</Text>
      </TouchableOpacity>

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
                style={styles.buyButton}
                onPress={() => openBuyModal(item.ticker)}
              >
                <Text style={styles.buyButtonTextText}>Buy</Text>
              </TouchableOpacity>
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
            {selectedTicker && (
              <Text style={styles.modalText}>
                You own: {selectedAssetQuantity?.toFixed(5) ?? 'N/A'} {selectedTicker}
              </Text>
            )}
            <Text style={styles.modalText}>
              Enter the quantity of {selectedTicker} to sell:
            </Text>
            <TextInput
              style={styles.modalInput}
              placeholder="Quantity"
              keyboardType="numeric"
              value={sellQuantity}
              onChangeText={(val) => {
                setSellQuantity(val);
                setSellModalError('');
              }}
            />
            {sellModalError ? (
              <Text style={styles.modalErrorText}>{sellModalError}</Text>
            ) : null}
            <View style={styles.modalButtonContainer}>
              <TouchableOpacity style={styles.modalButton} onPress={handleSellAsset}>
                <Text style={styles.modalButtonText}>Sell</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.modalButton, styles.sellAllButton]}
                onPress={handleSellAll}
              >
                <Text style={styles.modalButtonText}>Sell All</Text>
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

      {/* Buy Modal */}
      <Modal
        visible={buyModalVisible}
        animationType="slide"
        transparent={true}
        onRequestClose={closeBuyModal}
      >
        <View style={styles.modalBackground}>
          <View style={styles.modalContainer}>
            <Text style={styles.modalTitle}>Buy Asset</Text>
            <Text style={styles.modalText}>
              Enter the USD amount to invest in {selectedTicker}:
            </Text>
            <TextInput
              style={styles.modalInput}
              placeholder="USD Amount"
              keyboardType="numeric"
              value={buyUsdQuantity}
              onChangeText={setBuyUsdQuantity}
            />
            <View style={styles.modalButtonContainer}>
              <TouchableOpacity style={styles.modalButton} onPress={handleBuyAsset}>
                <Text style={styles.modalButtonText}>Buy</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.modalButton, styles.modalCancelButton]}
                onPress={closeBuyModal}
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
                <Text style={styles.modalText}>Asset: {selectedReport.ticker}</Text>
                <Text style={styles.modalText}>
                  Profit: ${selectedReport.profit?.toFixed(2) ?? 'N/A'}
                </Text>
                <Text style={styles.modalText}>
                  Total Invested: ${selectedReport.total_usd_invested?.toFixed(2) ?? 'N/A'}
                </Text>
              </View>
            )}
            <TouchableOpacity
              style={[styles.modalButton, styles.modalCloseButton]}
              onPress={() => setReportModalVisible(false)}
            >
              <Text style={styles.modalButtonText}>Close</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </View>
  );
};

// ----------------------------
// Styles
// ----------------------------
const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#f5f5f5',
    width: '100%',
    position: 'relative', // So we can position the button top-right
  },
  downloadCsvButton: {
    position: 'absolute',
    top: 20,
    right: 20,
    backgroundColor: 'green',
    paddingVertical: 10,
    paddingHorizontal: 15,
    borderRadius: 5,
    zIndex: 10,
  },
  downloadCsvButtonText: {
    color: '#fff',
    fontWeight: 'bold',
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
  errorText: {
    color: 'red',
    marginBottom: 10,
    textAlign: 'center',
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
    width: 250,
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
  buyButton: {
    backgroundColor: '#28a745',
    paddingVertical: 5,
    paddingHorizontal: 10,
    borderRadius: 5,
  },
  buyButtonTextText: {
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
  emptyText: {
    textAlign: 'center',
    color: '#aaa',
    marginTop: 20,
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
    width: '50%',
    alignItems: 'center',
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 10,
    textAlign: 'center',
  },
  modalText: {
    fontSize: 16,
    marginBottom: 10,
    textAlign: 'center',
  },
  modalErrorText: {
    color: 'red',
    textAlign: 'center',
    marginBottom: 10,
    fontWeight: 'bold',
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
    flexWrap: 'wrap',
    justifyContent: 'space-around',
    width: '100%',
  },
  modalButton: {
    backgroundColor: '#007bff',
    padding: 10,
    borderRadius: 5,
    minWidth: '25%',
    alignItems: 'center',
    marginTop: 5,
  },
  modalCancelButton: {
    backgroundColor: '#ccc',
  },
  modalButtonText: {
    color: 'white',
    fontWeight: 'bold',
    textAlign: 'center',
  },
  sellAllButton: {
    backgroundColor: '#FF8C00',
  },
  modalCloseButton: {
    backgroundColor: '#007bff',
    marginTop: 10,
    width: '40%',
  },
});

export default MyAssets;
