import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import {
  PaymentElement,
  useStripe,
  useElements,
} from "@stripe/react-stripe-js";

export default function CheckoutForm() {
  const stripe = useStripe();
  const elements = useElements();
  const location = useLocation();
  const navigate = useNavigate();
  
  // Destructure amount along with clientSecret from state
  const { clientSecret, amount } = location.state || {};

  const [message, setMessage] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!clientSecret) {
      setMessage("Missing payment details. Please start the payment process again.");
      console.log("No Client Secret!");
    }
  }, [clientSecret]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!stripe || !elements) {
      // Stripe.js hasn't loaded yet.
      return;
    }

    setIsLoading(true);

    const result = await stripe.confirmPayment({
      elements,
      confirmParams: {
        // Optionally, include the amount in the payment confirmation if needed
        return_url: `${window.location.origin}/complete`,
      },
    });

    if (result.error) {
      if (result.error.type === "card_error" || result.error.type === "validation_error") {
        setMessage(result.error.message || "An error occurred.");
      } else {
        setMessage("An unexpected error occurred.");
      }
    } else if (result.paymentIntent) {
      navigate("/complete", {
        state: {
          paymentIntentId: result.paymentIntent.id,
          status: result.paymentIntent.status,
        },
      });
    } else {
      setMessage("Unexpected result. Please try again.");
    }

    setIsLoading(false);
  };

  const paymentElementOptions = {
    layout: {
      type: "accordion", // Accordion layout for a cleaner UI
    },
    style: {
      base: {
        fontSize: "18px", // Larger font size for better readability
        color: "#32325d",
        "::placeholder": {
          color: "#aab7c4",
        },
      },
      invalid: {
        color: "#fa755a",
        iconColor: "#fa755a",
      },
    },
  };

  if (!clientSecret) {
    return <div>Loading payment details...</div>;
  }

  return (
    <div style={styles.container}>
      <form id="payment-form" onSubmit={handleSubmit} style={styles.form}>
        <div style={styles.paymentElementContainer}>
          <PaymentElement id="payment-element" options={paymentElementOptions} />
        </div>
        <button 
          type="submit" 
          disabled={isLoading || !stripe || !elements} 
          style={styles.button}
        >
          {isLoading ? <span>Loading...</span> : `Pay Now $${amount?.toFixed(2) || '0.00'}`}
        </button>
        {message && <div id="payment-message" style={styles.message}>{message}</div>}
      </form>
    </div>
  );
}

const styles = {
  container: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    justifyContent: "center",
    height: "100vh",
    backgroundColor: "#f9f9f9",
    width: "100%",
  },
  form: {
    width: "600px", // Wider form for better usability
    padding: "30px",
    borderRadius: "10px",
    backgroundColor: "#fff",
    boxShadow: "0px 4px 12px rgba(0, 0, 0, 0.15)",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    width: "45%",
  },
  paymentElementContainer: {
    width: "100%", // Ensure the payment element takes up full width
    marginBottom: "20px",
  },
  button: {
    padding: "15px 30px",
    fontSize: "16px",
    color: "#fff",
    backgroundColor: "#27ae60",
    border: "none",
    borderRadius: "10px",
    cursor: "pointer",
    width: "100%",
    textAlign: "center",
    transition: "background-color 0.3s ease",
  },
  message: {
    marginTop: "10px",
    color: "#e74c3c",
    textAlign: "center",
  },
};
