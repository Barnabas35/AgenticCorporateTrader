import React, { createContext, useState, useContext, ReactNode, useEffect } from 'react';

interface UserContextType {
  username: string;
  setUsername: (username: string) => void; // Setter for username
  email: string;
  setEmail: (email: string) => void; // Setter for email
  profileIconUrl: string;
  setProfileIconUrl: (url: string) => void; // Setter for profile icon
  sessionToken: string | null;
  setSessionToken: (token: string | null) => void; // Setter for session token
}

const UserContext = createContext<UserContextType | undefined>(undefined);

interface UserProviderProps {
  children: ReactNode;
}

export const UserProvider: React.FC<UserProviderProps> = ({ children }) => {
  // Initializing state with localStorage values if they exist
  const [username, setUsername] = useState<string>(() => {
    return localStorage.getItem('username') || '';
  });
  const [email, setEmail] = useState<string>(() => {
    return localStorage.getItem('email') || '';
  });
  const [profileIconUrl, setProfileIconUrl] = useState<string>(() => {
    return localStorage.getItem('profileIconUrl') || '';
  });
  const [sessionToken, setSessionToken] = useState<string | null>(() => {
    return localStorage.getItem('sessionToken');
  });

  // Update localStorage whenever any user state changes
  useEffect(() => {
    if (username) {
      localStorage.setItem('username', username);
    } else {
      localStorage.removeItem('username');
    }
  }, [username]);

  useEffect(() => {
    if (email) {
      localStorage.setItem('email', email);
    } else {
      localStorage.removeItem('email');
    }
  }, [email]);

  useEffect(() => {
    if (profileIconUrl) {
      localStorage.setItem('profileIconUrl', profileIconUrl);
    } else {
      localStorage.removeItem('profileIconUrl');
    }
  }, [profileIconUrl]);

  useEffect(() => {
    if (sessionToken) {
      localStorage.setItem('sessionToken', sessionToken);
    } else {
      localStorage.removeItem('sessionToken');
    }
  }, [sessionToken]);

  // Value to provide to the context
  const value = {
    username,
    setUsername, 
    email,
    setEmail, 
    profileIconUrl,
    setProfileIconUrl, 
    sessionToken,
    setSessionToken, 
  };

  return <UserContext.Provider value={value}>{children}</UserContext.Provider>;
};

// Hook to use the UserContext
export const useUser = () => {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error('useUser must be used within a UserProvider');
  }
  return context; // Return the context
};

// Custom hooks for individual pieces of user data
export const useUsername = () => {
  const { username, setUsername } = useUser();
  return [username, setUsername] as const; // Return username and its setter
};

export const useEmail = () => {
  const { email, setEmail } = useUser();
  return [email, setEmail] as const; // Return email and its setter
};

export const useProfileIconUrl = () => {
  const { profileIconUrl, setProfileIconUrl } = useUser();
  return [profileIconUrl, setProfileIconUrl] as const; // Return profile icon URL and its setter
};

export const useSessionToken = () => {
  const { sessionToken, setSessionToken } = useUser();
  return [sessionToken, setSessionToken] as const; // Return session token and its setter
};
