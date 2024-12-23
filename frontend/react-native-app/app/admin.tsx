import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  TextInput,
  ScrollView,
  ActivityIndicator,
  Modal,
} from 'react-native';
import { useSessionToken } from '../components/userContext';

interface SupportTicket {
  issue_subject: string;
  user_id: string;
  issue_description: string;
  issue_status: 'open' | 'resolved';
  ticket_id: string;
  unix_timestamp: number;
}

interface User {
  email: string;
  id: string;
  password: string;
  session_token: string;
  user_type: string;
  username: string;
}

const AdminPage: React.FC = () => {
  const [sessionToken] = useSessionToken();
  const [tickets, setTickets] = useState<SupportTicket[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [view, setView] = useState<'tickets' | 'users'>('tickets');
  const [ticketView, setTicketView] = useState<'open' | 'resolved'>('open');
  const [expandedTicket, setExpandedTicket] = useState<SupportTicket | null>(null);
  const [responseSubject, setResponseSubject] = useState<string>('');
  const [responseBody, setResponseBody] = useState<string>('');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [confirmDeleteModalVisible, setConfirmDeleteModalVisible] = useState<boolean>(false);
  const [userToDelete, setUserToDelete] = useState<User | null>(null);
  const [infoModalVisible, setInfoModalVisible] = useState<boolean>(false);
  const [infoModalMessage, setInfoModalMessage] = useState<string>('');

  useEffect(() => {
    fetchSupportTickets();
    fetchUsers();
  }, []);

  const fetchSupportTickets = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch('https://tradeagently.dev/get-support-ticket-list', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ session_token: sessionToken }),
      });
      const data = await response.json();
      if (data.status === 'Success') {
        setTickets(data.support_tickets);
      } else {
        setError(data.message || 'Failed to fetch tickets.');
      }
    } catch (err) {
      console.error('Error fetching tickets:', err);
      setError('Failed to fetch tickets.');
    } finally {
      setLoading(false);
    }
  };

  const resolveTicket = async (ticket: SupportTicket) => {
    if (!responseSubject.trim() || !responseBody.trim()) {
      setError('Please provide a subject and body for the response.');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const response = await fetch('https://tradeagently.dev/resolve-support-ticket', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session_token: sessionToken,
          ticket_id: ticket.ticket_id,
          response_subject: responseSubject,
          response_body: responseBody,
        }),
      });
      const data = await response.json();

      if (data.status === 'Success') {
        setInfoModalMessage('Ticket resolved successfully.');
        setInfoModalVisible(true);

        setResponseSubject('');
        setResponseBody('');
        setExpandedTicket(null);

        fetchSupportTickets();
      } else {
        setError(data.message || 'Failed to resolve ticket.');
      }
    } catch (err) {
      console.error('Error resolving ticket:', err);
      setError('Failed to resolve ticket.');
    } finally {
      setLoading(false);
    }
  };

  const fetchUsers = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetch('https://tradeagently.dev/get-user-list', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ session_token: sessionToken }),
      });
      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }
      const data = await response.json();

      if (data.status === 'Success') {
        setUsers(data.user_list);
      } else {
        setError(data.message || 'Failed to fetch users.');
      }
    } catch (err) {
      console.error('Error fetching users:', err);
      setError('Failed to fetch users. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const triggerDeleteUser = (user: User) => {
    setUserToDelete(user);
    setConfirmDeleteModalVisible(true);
  };

  const confirmDeleteUser = async () => {
    if (!userToDelete) return;
    setConfirmDeleteModalVisible(false);
    setLoading(true);
    setError(null);

    try {
      const response = await fetch('https://tradeagently.dev/admin-delete-user', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          session_token: sessionToken,
          id: userToDelete.id,
        }),
      });
      const data = await response.json();

      if (data.status === 'Success') {
        setInfoModalMessage(`User "${userToDelete.username}" deleted successfully.`);
        setInfoModalVisible(true);
        fetchUsers();
      } else {
        setError(data.message || 'Failed to delete user.');
      }
    } catch (err) {
      console.error('Error deleting user:', err);
      setError('Failed to delete user.');
    } finally {
      setUserToDelete(null);
      setLoading(false);
    }
  };

  const filteredUsers = users.filter(
    (user) =>
      user.username.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.email.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <View style={styles.container}>
      {loading && <ActivityIndicator size="large" color="#0000ff" style={{ marginBottom: 10 }} />}
      {error && <Text style={styles.errorText}>{error}</Text>}

      {/* Navigation Buttons */}
      <View style={styles.centerContainer}>
        <View style={styles.navContainer}>
          <TouchableOpacity
            style={[styles.smallButton, view === 'tickets' ? styles.activeButton : {}]}
            onPress={() => setView('tickets')}
          >
            <Text style={styles.buttonText}>Tickets</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.smallButton, view === 'users' ? styles.deleteUsersButton : {}]}
            onPress={() => setView('users')}
          >
            <Text style={styles.buttonText}>Delete Users</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* ----------------- Tickets View ----------------- */}
      {view === 'tickets' && (
        <View>
          <View style={styles.ticketNavContainer}>
            <TouchableOpacity
              style={[styles.smallButton, ticketView === 'open' ? styles.openTicketButton : {}]}
              onPress={() => setTicketView('open')}
            >
              <Text style={styles.buttonText}>Open Tickets</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.smallButton, ticketView === 'resolved' ? styles.resolvedTicketButton : {}]}
              onPress={() => setTicketView('resolved')}
            >
              <Text style={styles.buttonText}>Resolved Tickets</Text>
            </TouchableOpacity>
          </View>

          {expandedTicket ? (
            <View style={styles.ticketItem}>
              <Text style={styles.ticketTitle}>{expandedTicket.issue_subject}</Text>
              <Text style={styles.ticketDescription}>{expandedTicket.issue_description}</Text>
              <TextInput
                style={styles.responseInput}
                placeholder="Response Subject"
                value={responseSubject}
                onChangeText={setResponseSubject}
              />
              <TextInput
                style={[styles.responseInput, { height: 80 }]}
                placeholder="Response Body"
                value={responseBody}
                onChangeText={setResponseBody}
                multiline
              />
              <TouchableOpacity
                style={styles.button}
                onPress={() => resolveTicket(expandedTicket)}
              >
                <Text style={styles.buttonText}>Submit Response</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.smallButton, styles.cancelButton]}
                onPress={() => {
                  setExpandedTicket(null);
                  setResponseSubject('');
                  setResponseBody('');
                }}
              >
                <Text style={styles.buttonText}>Cancel</Text>
              </TouchableOpacity>
            </View>
          ) : (
            <FlatList
              data={tickets.filter((ticket) => ticket.issue_status === ticketView)}
              renderItem={({ item }) => (
                <View style={styles.ticketItem}>
                  <Text style={styles.ticketTitle}>{item.issue_subject}</Text>
                  <Text style={styles.ticketDescription}>{item.issue_description}</Text>

                  {ticketView === 'open' && (
                    <TouchableOpacity
                      style={[styles.smallButton, styles.expandButton]}
                      onPress={() => setExpandedTicket(item)}
                    >
                      <Text style={styles.buttonText}>Resolve Ticket</Text>
                    </TouchableOpacity>
                  )}
                </View>
              )}
              keyExtractor={(item) => item.ticket_id}
            />
          )}
        </View>
      )}

      {/* ----------------- Users View
       ----------------- */}
      {view === 'users' && (
        <ScrollView>
          <TextInput
            style={styles.searchInput}
            placeholder="Search users by username or email..."
            value={searchQuery}
            onChangeText={setSearchQuery}
          />
          <FlatList
            data={filteredUsers}
            keyExtractor={(item) => item.id}
            renderItem={({ item }) => (
              <View style={styles.userItem}>
                <Text style={styles.userText}>Username: {item.username}</Text>
                <Text style={styles.userText}>Email: {item.email}</Text>
                <Text style={[styles.userText, { color: '#999' }]}>ID: {item.id}</Text>

                <TouchableOpacity
                  style={[styles.button, styles.narrowDeleteButton]}
                  onPress={() => triggerDeleteUser(item)}
                >
                  <Text style={styles.buttonText}>Delete User</Text>
                </TouchableOpacity>
              </View>
            )}
          />
        </ScrollView>
      )}

      {/* ------------- CONFIRM DELETE USER MODAL ------------- */}
      <Modal
        visible={confirmDeleteModalVisible}
        transparent
        animationType="slide"
        onRequestClose={() => setConfirmDeleteModalVisible(false)}
      >
        <View style={styles.modalBackground}>
          <View style={styles.modalContainer}>
            <Text style={styles.modalTitle}>Confirm Deletion</Text>
            {userToDelete && (
              <Text style={styles.modalMessage}>
                Are you sure you want to delete user: "{userToDelete.username}"?
              </Text>
            )}
            <View style={styles.modalButtonRow}>
              <TouchableOpacity
                style={[styles.button, { backgroundColor: '#FF5555', marginRight: 10 }]}
                onPress={confirmDeleteUser}
              >
                <Text style={styles.buttonText}>Yes</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.button, { backgroundColor: '#ccc' }]}
                onPress={() => {
                  setConfirmDeleteModalVisible(false);
                  setUserToDelete(null);
                }}
              >
                <Text style={styles.buttonText}>No</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>

      {/* ------------- INFO/SUCCESS MODAL ------------- */}
      <Modal
        visible={infoModalVisible}
        transparent
        animationType="slide"
        onRequestClose={() => setInfoModalVisible(false)}
      >
        <View style={styles.modalBackground}>
          <View style={styles.modalContainer}>
            <Text style={styles.modalTitle}>Info</Text>
            <Text style={styles.modalMessage}>{infoModalMessage}</Text>
            <TouchableOpacity
              style={[styles.button, { marginTop: 20 }]}
              onPress={() => setInfoModalVisible(false)}
            >
              <Text style={styles.buttonText}>OK</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </View>
  );
};

export default AdminPage;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
    backgroundColor: '#f5f5f5',
    width: '100%',
  },
  centerContainer: {
    alignItems: 'center',
    marginBottom: 10,
  },
  navContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
  },
  smallButton: {
    padding: 8,
    marginHorizontal: 5,
    borderRadius: 5,
    alignItems: 'center',
    width: 190,
    backgroundColor: '#ccc',
  },
  activeButton: {
    backgroundColor: '#0056b3',
  },
  deleteUsersButton: {
    backgroundColor: '#FF5555',
  },
  openTicketButton: {
    backgroundColor: '#ffa500',
  },
  resolvedTicketButton: {
    backgroundColor: '#4caf50',
  },
  expandButton: {
    backgroundColor: '#007bff',
    marginTop: 10,
  },
  cancelButton: {
    backgroundColor: '#ff6347',
    marginTop: 10,
  },
  ticketNavContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    marginBottom: 10,
  },
  ticketItem: {
    padding: 10,
    backgroundColor: '#fff',
    borderRadius: 5,
    marginBottom: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  ticketTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  ticketDescription: {
    fontSize: 14,
    color: '#555',
  },
  responseInput: {
    height: 40,
    borderColor: '#ddd',
    borderWidth: 1,
    borderRadius: 5,
    marginVertical: 10,
    padding: 8,
  },
  button: {
    backgroundColor: '#007bff',
    padding: 10,
    borderRadius: 5,
    alignItems: 'center',
  },
  buttonText: {
    color: '#3B3B3B',
    fontWeight: 'bold',
  },
  userItem: {
    padding: 10,
    backgroundColor: '#fff',
    borderRadius: 5,
    marginBottom: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  userText: {
    fontSize: 14,
  },
  narrowDeleteButton: {
    backgroundColor: '#FF5555',
    marginTop: 10,
    width: 100,
  },
  errorText: {
    color: 'red',
    marginBottom: 10,
  },
  searchInput: {
    height: 40,
    borderColor: '#ddd',
    borderWidth: 1,
    borderRadius: 5,
    marginVertical: 10,
    padding: 8,
  },
  // Modals
  modalBackground: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalContainer: {
    width: 300,
    backgroundColor: '#fff',
    padding: 16,
    borderRadius: 10,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 10,
    textAlign: 'center',
  },
  modalMessage: {
    fontSize: 16,
    color: '#333',
    textAlign: 'center',
  },
  modalButtonRow: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    marginTop: 20,
  },
});
