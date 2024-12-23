import React, { useEffect, useState } from "react";
import { useStripe } from "@stripe/react-stripe-js";

const STATUS_CONTENT_MAP = {
  succeeded: {
    text: "Payment succeeded",
    iconColor: "#30B130",
    icon: "✔️",
  },
  processing: {
    text: "Your payment is processing.",
    iconColor: "#6D6E78",
    icon: "⏳",
  },
  requires_payment_method: {
    text: "Your payment was not successful, please try again.",
    iconColor: "#DF1B41",
    icon: "❌",
  },
  default: {
    text: "Something went wrong, please try again.",
    iconColor: "#DF1B41",
    icon: "⚠️",
  },
};

export default function CompletePage() {
  const stripe = useStripe();
  const [status, setStatus] = useState(null);
  const [intentId, setIntentId] = useState(null);
  const [fetched, setFetched] = useState(false); 

  useEffect(() => {
    if (!stripe) return;

    const clientSecret = new URLSearchParams(window.location.search).get(
      "payment_intent_client_secret"
    );

    if (!clientSecret) {
      setStatus("default");
      const timer = setTimeout(() => setFetched(true), 1000);
      return () => clearTimeout(timer);
    }

    // Retrieve the PaymentIntent using the client secret
    stripe
      .retrievePaymentIntent(clientSecret)
      .then(({ paymentIntent }) => {
        if (paymentIntent) {
          setStatus(paymentIntent.status);
          setIntentId(paymentIntent.id);
        } else {
          setStatus("default");
        }
        const timer = setTimeout(() => setFetched(true), 1000);
        return () => clearTimeout(timer);
      })
      .catch((error) => {
        console.error("Error retrieving payment intent:", error);
        setStatus("default");

        const timer = setTimeout(() => setFetched(true), 1000);
        return () => clearTimeout(timer);
      });
  }, [stripe]);

  return (
    <div style={styles.outerContainer}>
      <div style={styles.container}>
        {!fetched ? (
          <div style={styles.loadingContainer}>
            <div style={styles.spinner}></div>
            <p style={styles.loadingText}>Loading...</p>
          </div>
        ) : (
          <>
            {/* Status Icon */}
            <div
              style={{
                ...styles.iconContainer,
                backgroundColor: STATUS_CONTENT_MAP[status]?.iconColor,
              }}
            >
              {STATUS_CONTENT_MAP[status]?.icon}
            </div>

            {/* Status Text */}
            <h2 style={styles.statusText}>{STATUS_CONTENT_MAP[status]?.text}</h2>

            {/* Payment Intent Details */}
            {intentId && (
              <div style={styles.detailsTable}>
                <div style={styles.row}>
                </div>
              </div>
            )}

            {/* Conditional Button */}
            {status === "succeeded" ? (
              <a href="/" style={styles.homeButton}>
                Return to Home Page
              </a>
            ) : (
              <a href="/checkout" style={styles.retryButton}>
                Try Another Payment
              </a>
            )}
          </>
        )}
      </div>
    </div>
  );
}

const styles = {
  outerContainer: {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    height: "100vh", 
    width: "100vw", 
    backgroundColor: "#f2f2f2", 
    fontFamily: "'Poppins', sans-serif", 
  },
  container: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    justifyContent: "center",
    padding: "30px",
    backgroundColor: "#fff", 
    borderRadius: "15px", 
    boxShadow: "0px 4px 12px rgba(0, 0, 0, 0.1)", 
    width: "400px",
    textAlign: "center", 
  },
  loadingContainer: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    justifyContent: "center",
  },
  loadingText: {
    fontSize: "18px",
    color: "#555",
    fontFamily: "'Poppins', sans-serif",
    marginTop: "10px",
  },
  spinner: {
    border: "4px solid #f3f3f3", 
    borderTop: "4px solid #3498db", 
    borderRadius: "50%",
    width: "40px",
    height: "40px",
    animation: "spin 2s linear infinite",
  },
  iconContainer: {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    width: "60px",
    height: "60px",
    borderRadius: "50%",
    marginBottom: "20px",
    fontSize: "30px",
    color: "#fff",
  },
  statusText: {
    fontSize: "20px",
    fontWeight: "bold",
    marginBottom: "20px",
    color: "#333",
    fontFamily: "'Poppins', sans-serif", 
  },
  detailsTable: {
    width: "100%",
    marginBottom: "20px",
    fontFamily: "'Poppins', sans-serif", 
  },
  row: {
    display: "flex",
    justifyContent: "space-between",
    marginBottom: "10px",
  },
  label: {
    fontWeight: "bold",
  },
  content: {
    color: "#555",
  },
  retryButton: {
    display: "inline-block",
    padding: "10px 20px",
    fontSize: "16px",
    fontWeight: "bold",
    color: "#fff",
    backgroundColor: "#27ae60", 
    borderRadius: "10px",
    textDecoration: "none",
    cursor: "pointer",
    transition: "background-color 0.3s ease",
    boxShadow: "0px 4px 6px rgba(0, 0, 0, 0.1)",
    marginTop: "10px",
  },
  homeButton: {
    display: "inline-block",
    padding: "10px 20px",
    fontSize: "16px",
    fontWeight: "bold",
    color: "#fff",
    backgroundColor: "#007bff", 
    borderRadius: "10px",
    textDecoration: "none",
    cursor: "pointer",
    transition: "background-color 0.3s ease",
    boxShadow: "0px 4px 6px rgba(0, 0, 0, 0.1)",
    marginTop: "10px",
  },
};
